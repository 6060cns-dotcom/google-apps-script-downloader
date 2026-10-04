(function () {
    const wasmSizes = window.__WASM_SIZES__ || {};
    const originalFetch = window.fetch.bind(window);
    const originalInstantiateStreaming = WebAssembly.instantiateStreaming
        ? WebAssembly.instantiateStreaming.bind(WebAssembly)
        : null;
    const originalCompileStreaming = WebAssembly.compileStreaming
        ? WebAssembly.compileStreaming.bind(WebAssembly)
        : null;

    const progressMap = new Map();
    let finished = false;
    let sawWasm = false;

    function fileNameOf(url) {
        return url.split("/").pop().split("?")[0].split("#")[0];
    }

    function resourceUrl(resource) {
        if (typeof resource === "string") return resource;
        if (resource instanceof URL) return resource.href;
        if (resource && typeof resource.url === "string") return resource.url;
        return "";
    }

    function isWasmUrl(url) {
        const name = fileNameOf(url);
        return name.endsWith(".wasm") || Object.prototype.hasOwnProperty.call(wasmSizes, name);
    }

    function knownSize(url) {
        const name = fileNameOf(url);
        return wasmSizes[name] || 0;
    }

    function dismiss() {
        if (finished) return;
        finished = true;
        window.fetch = originalFetch;
        if (originalInstantiateStreaming) {
            WebAssembly.instantiateStreaming = originalInstantiateStreaming;
        }
        if (originalCompileStreaming) {
            WebAssembly.compileStreaming = originalCompileStreaming;
        }
        const loader = document.getElementById("wasm_loader");
        if (!loader) return;
        loader.classList.add("is-done");
        loader.setAttribute("aria-busy", "false");
        setTimeout(function () {
            loader.remove();
        }, 480);
    }

    function updateUI() {
        const loader = document.getElementById("wasm_loader");
        const fill = document.getElementById("wasm_progress_fill");
        const status = document.getElementById("wasm_status");
        const bar = document.querySelector(".loader-bar");
        if (!loader) return;

        let total = 0;
        let loaded = 0;
        let allHaveTotal = true;
        progressMap.forEach(function (entry) {
            loaded += entry.loaded;
            if (entry.total > 0) total += entry.total;
            else allHaveTotal = false;
        });

        if (allHaveTotal && total > 0) {
            loader.classList.remove("is-indeterminate");
            const percent = Math.min(100, Math.round((loaded / total) * 100));
            if (fill) fill.style.width = percent + "%";
            if (bar) bar.setAttribute("aria-valuenow", String(percent));
            if (status) status.textContent = percent + "%";
            if (percent >= 100) setTimeout(dismiss, 320);
            return;
        }

        loader.classList.add("is-indeterminate");
        if (status) status.textContent = "טוען…";
    }

    function trackResponse(url, response) {
        if (!response || !response.body || !response.ok) return response;
        const total =
            knownSize(url) ||
            parseInt(response.headers.get("content-length"), 10) ||
            0;
        let loaded = 0;
        progressMap.set(url, { loaded: 0, total: total });
        sawWasm = true;
        updateUI();

        const reader = response.body.getReader();
        return new Response(
            new ReadableStream({
                async start(controller) {
                    try {
                        while (true) {
                            const chunk = await reader.read();
                            if (chunk.done) break;
                            loaded += chunk.value.byteLength;
                            progressMap.set(url, { loaded: loaded, total: total });
                            updateUI();
                            controller.enqueue(chunk.value);
                        }
                        if (total === 0) {
                            progressMap.set(url, { loaded: loaded, total: loaded });
                            updateUI();
                        }
                        controller.close();
                    } catch (error) {
                        controller.error(error);
                    }
                },
            }),
            { status: response.status, statusText: response.statusText, headers: response.headers },
        );
    }

    async function wrapSource(source) {
        const resolved = await source;
        if (!(resolved instanceof Response)) return resolved;
        const url = resolved.url || "";
        if (progressMap.has(url)) return resolved;
        if (!isWasmUrl(url)) return resolved;
        return trackResponse(url, resolved);
    }

    window.fetch = async function (resource, config) {
        const url = resourceUrl(resource);
        const response = await originalFetch(resource, config);
        if (!isWasmUrl(url)) return response;
        return trackResponse(url, response);
    };

    if (originalInstantiateStreaming) {
        WebAssembly.instantiateStreaming = function (source, imports) {
            return originalInstantiateStreaming(wrapSource(source), imports);
        };
    }
    if (originalCompileStreaming) {
        WebAssembly.compileStreaming = function (source) {
            return originalCompileStreaming(wrapSource(source));
        };
    }

    const observer = new MutationObserver(function () {
        if (!document.querySelector("canvas")) return;
        observer.disconnect();
        setTimeout(dismiss, 200);
    });
    observer.observe(document.documentElement, { childList: true, subtree: true });
})();
