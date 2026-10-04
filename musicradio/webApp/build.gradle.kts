import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composePwa)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.compose.ui)
        }
    }
}

val generateWasmProgress =
    tasks.register("generateWasmProgress") {
        description = "Writes exact wasm file sizes so the splash can show real download progress"
        // Capture the DirectoryProperty, not `layout` / the script receiver: the configuration
        // cache cannot serialize those.
        val buildDir = layout.buildDirectory
        doLast {
            val root = buildDir.get().asFile
            val searchRoots =
                listOf("compileSync", "compose", "dist", "kotlin-webpack").map { root.resolve(it) }
            val wasmFiles =
                searchRoots
                    .filter { it.exists() }
                    .flatMap { dir -> dir.walkTopDown().filter { it.isFile && it.extension == "wasm" } }
                    .distinctBy { it.name }
            val sizes =
                wasmFiles.joinToString(",") { file ->
                    "\"${file.name}\":${file.length()}"
                }
            val js = "window.__WASM_SIZES__ = {$sizes};\n"
            listOf(
                root.resolve("processedResources/wasmJs/main"),
                root.resolve("dist/wasmJs/productionExecutable"),
            ).forEach { dir ->
                dir.mkdirs()
                dir.resolve("wasm_sizes.js").writeText(js)
            }
        }
    }

tasks.matching { it.name == "wasmJsBrowserDistribution" }.configureEach {
    finalizedBy(generateWasmProgress)
}

tasks.matching { it.name == "compileKotlinWasmJs" }.configureEach {
    finalizedBy(generateWasmProgress)
}
