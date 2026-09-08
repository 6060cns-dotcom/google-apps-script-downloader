// ==UserScript==
// @name         Google Apps Script - הורדת קבצי הפרויקט
// @namespace    https://mitmachim.top/user/לומדעס
// @version      1.5
// @description  הורדת קבצי GS, HTML והמניפסט ישירות מעורך Google Apps Script בלחיצה אחת
// @author       לומדעס
// @match        https://script.google.com/*
// @run-at       document-idle
// @icon         https://www.gstatic.com/script/apps_script_1x_48dp.png
// @grant        none
// @inject-into  page
// @updateURL    https://raw.githubusercontent.com/6060cns-dotcom/google-apps-script-downloader/main/Apps-Script-Downloader-Tampermonkey.user.js
// @downloadURL  https://raw.githubusercontent.com/6060cns-dotcom/google-apps-script-downloader/main/Apps-Script-Downloader-Tampermonkey.user.js
// ==/UserScript==

(()=>{
'use strict';

const sleep = ms => new Promise(r => setTimeout(r, ms));
const clean = s => String(s || '').replace(/[\u200e\u200f]/g, '').trim();
const safe = s => clean(s).replace(/[\\/:*?"<>|]+/g, '_').replace(/\s+/g, ' ').slice(0, 120) || 'code.gs';

// יצירת אלמנטים באופן נקי ללא innerHTML (עקיפת Trusted Types)
const E = (tag, text = null, id = null, style = null) => {
  const e = document.createElement(tag);
  if (text != null) e.textContent = text;
  if (id) e.id = id;
  if (style) e.style.cssText = style;
  return e;
};

const visible = el => {
  if (!el) return false;
  const r = el.getBoundingClientRect(), s = getComputedStyle(el);
  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';
};

const extractName = el => {
  const raw = clean(el.getAttribute('aria-label') || el.getAttribute('title') || el.innerText || el.textContent);
  const matches = [...raw.matchAll(/([^\n]+?\.(?:gs|html|json))(?=\n|$)/ig)];
  return matches.length === 1 ? safe(matches[0][1]) : '';
};

// איתור שמות הקבצים
const discoverFiles = (panel) => {
  const result = new Map();
  const candidates = document.querySelectorAll('[role="treeitem"], div[title], span[title], .as-file-list-item');
  const pool = candidates.length > 0 ? candidates : document.querySelectorAll('*');

  for (const el of pool) {
    if (panel && panel.contains(el)) continue;
    if (!visible(el)) continue;
    const name = extractName(el);
    if (!name) continue;
    const r = el.getBoundingClientRect(), score = r.width * r.height;
    const old = result.get(name);
    if (!old || score < old.score) result.set(name, { el, score });
  }
  return [...result].map(([name, x]) => ({ name, el: x.el, status: 'ממתין' }));
};

// גישה ישירה ל-Monaco Editor (פועל כי @grant none מזריק ישירות לעמוד)
const monacoContexts = () => {
  const found = [];
  const visit = w => {
    try {
      if (w.monaco && w.monaco.editor) found.push(w);
      for (let i = 0; i < w.frames.length; i++) visit(w.frames[i]);
    } catch (_) {}
  };
  visit(window);
  return found;
};

const readMonaco = (wanted) => {
  for (const w of monacoContexts()) {
    const api = w.monaco.editor;
    const editors = typeof api.getEditors === 'function' ? api.getEditors() : [];
    const candidates = [...editors].sort((a,b) => {
      try { return Number(b.hasTextFocus()) - Number(a.hasTextFocus()); } catch (_) { return 0; }
    });
    for (const ed of candidates) {
      try {
        const model = ed.getModel();
        if (!model) continue;
        const uri = String(model.uri || '');
        if (uri.toLowerCase().includes(wanted.toLowerCase().replace(/\.(gs|html)$/i, '')) || ed.hasTextFocus() || (ed.getDomNode() && visible(ed.getDomNode()))) {
          return { value: model.getValue(), source: 'Monaco editor', uri };
        }
      } catch (_) {}
    }
    const models = typeof api.getModels === 'function' ? api.getModels() : [];
    for (const model of models) {
      try {
        const uri = String(model.uri || '');
        if (uri.toLowerCase().includes(wanted.toLowerCase().replace(/\.(gs|html)$/i, ''))) {
          return { value: model.getValue(), source: 'Monaco model', uri };
        }
      } catch (_) {}
    }
  }
  return null;
};

const readClipboard = async (panel) => {
  const area = [...document.querySelectorAll('.monaco-editor textarea,textarea.inputarea,[role="textbox"]')].find(el => (!panel || !panel.contains(el)) && visible(el));
  if (!area) return null;
  area.focus();
  area.dispatchEvent(new KeyboardEvent('keydown', { key: 'a', code: 'KeyA', ctrlKey: true, bubbles: true, cancelable: true }));
  area.dispatchEvent(new KeyboardEvent('keyup', { key: 'a', code: 'KeyA', ctrlKey: true, bubbles: true, cancelable: true }));
  await sleep(100);
  try {
    document.execCommand('copy');
    await sleep(100);
    const value = await navigator.clipboard.readText();
    return (value != null && value.trim().length > 0) ? { value, source: 'Clipboard', uri: '' } : null;
  } catch (_) {
    return null;
  }
};

const isSelectedFile = f => {
  let e = f.el;
  for (let i = 0; e && i < 6; i++, e = e.parentElement) {
    const cls = String(e.className || '').toLowerCase();
    if (e.getAttribute('aria-selected') === 'true' || e.getAttribute('aria-current') === 'true' || /(^|[\s_-])(selected|active)([\s_-]|$)/.test(cls)) {
      return true;
    }
  }
  return false;
};

const activateFile = async (f, panel) => {
  const r = f.el.getBoundingClientRect();
  const x = Math.max(0, Math.min(innerWidth - 1, r.left + r.width / 2));
  const y = Math.max(0, Math.min(innerHeight - 1, r.top + r.height / 2));
  const hit = document.elementFromPoint(x, y) || f.el;
  const candidates = [];
  for (const e of [hit, f.el, hit.parentElement, f.el.parentElement, hit.closest('[role="treeitem"]'), hit.closest('[role="button"]'), hit.closest('[tabindex]')]) {
    if (e && (!panel || !panel.contains(e)) && !candidates.includes(e)) candidates.push(e);
  }
  for (const e of candidates) {
    try {
      e.scrollIntoView({ block: 'nearest' });
      e.focus?.();
      for (const type of ['pointerdown', 'mousedown', 'pointerup', 'mouseup', 'click']) {
        e.dispatchEvent(new MouseEvent(type, { view: window, bubbles: true, cancelable: true, clientX: x, clientY: y, button: 0 }));
      }
      e.click?.();
      await sleep(150);
    } catch (_) {}
  }
};

const waitForContent = async (name, beforeSignature, allowSame, panel) => {
  let last = null, stable = 0;
  for (let attempt = 1; attempt <= 20; attempt++) {
    await sleep(attempt === 1 ? 400 : 200);
    const current = readMonaco(name) || await readClipboard(panel);
    if (current) {
      const sig = current.uri + '|' + current.value.length + '|' + current.value.slice(0, 60);
      const changed = !beforeSignature || sig !== beforeSignature;
      if (changed || allowSame) {
        if (last && current.value === last.value && current.uri === last.uri) stable++;
        else stable = 0;
        last = current;
        if (stable >= 2) return current;
      }
    }
  }
  return last;
};

const downloadOne = (name, text) => {
  const blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = name;
  (document.body || document.documentElement).appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 10000);
};

// פתיחת חלונית ההורדה והרצת התהליך השלם בלחיצה אחת
let isRunning = false;
const startProcess = async () => {
  if (isRunning) return;
  isRunning = true;

  const old = document.getElementById('gas-downloader-panel');
  if (old) old.remove();

  // בניית חלונית בסגנון Material Design
  const panel = E('section', null, 'gas-downloader-panel', `
    position: fixed;
    z-index: 2147483647;
    bottom: 75px;
    left: 20px;
    width: 380px;
    max-width: calc(100vw - 40px);
    max-height: 480px;
    display: flex;
    flex-direction: column;
    background: #ffffff;
    color: #202124;
    border: 1px solid #dadce0;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(60,64,67,0.25);
    font-family: 'Google Sans', Roboto, Arial, sans-serif;
    font-size: 13px;
    text-align: right;
    overflow: hidden;
  `);
  panel.dir = 'rtl';

  // Header
  const header = E('div', null, null, 'display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-bottom: 1px solid #dadce0; background: #f8f9fa;');
  const title = E('b', '📥 מוריד Apps Script', null, 'font-size: 14px; color: #1a73e8; font-weight: 500;');
  const closeBtn = E('button', '✕', null, 'border: none; background: transparent; font-size: 16px; cursor: pointer; color: #5f6368; padding: 2px 6px;');
  closeBtn.onclick = () => { panel.remove(); isRunning = false; };
  header.append(title, closeBtn);

  // Body
  const top = E('div', null, null, 'padding: 12px 16px 8px;');
  const stateEl = E('div', 'מאתר קבצים בפרויקט...', null, 'color: #3c4043; font-weight: 500; margin-bottom: 8px;');
  const track = E('div', null, null, 'height: 4px; background: #e8eaed; border-radius: 2px; overflow: hidden;');
  const barEl = E('div', null, null, 'height: 100%; width: 5%; background: #1a73e8; transition: width 0.25s;');
  track.append(barEl);
  top.append(stateEl, track);

  const filesEl = E('div', null, null, 'flex: 1; overflow-y: auto; padding: 4px 16px 12px; max-height: 220px; display: flex; flex-direction: column; gap: 4px;');

  panel.append(header, top, filesEl);
  (document.body || document.documentElement).appendChild(panel);

  const setProgress = (msg, val) => {
    stateEl.textContent = msg;
    barEl.style.width = Math.max(2, Math.min(100, val)) + '%';
  };

  const renderFiles = (files) => {
    filesEl.textContent = '';
    for (const f of files) {
      const row = E('div', null, null, 'display: flex; justify-content: space-between; align-items: center; font-size: 12px; padding: 4px 0; border-bottom: 1px dashed #f1f3f4;');
      const name = E('span', f.name, null, 'direction: ltr; font-family: monospace; color: #202124;');
      const status = E('span', f.status, null, `font-weight: 500; color: ${f.status.includes('הורד') ? '#137333' : f.status.includes('נכשל') ? '#d93025' : '#5f6368'};`);
      row.append(name, status);
      filesEl.append(row);
    }
  };

  await sleep(250);
  const files = discoverFiles(panel);
  renderFiles(files);

  if (!files.length) {
    setProgress('לא זוהו קבצים גלויים. פתח את סייר הקבצים בצד.', 0);
    isRunning = false;
    return;
  }

  const collected = [];
  for (let i = 0; i < files.length; i++) {
    const f = files[i];
    f.status = 'קורא תוכן...';
    renderFiles(files);
    setProgress(`קורא ומוריד (${i + 1}/${files.length}): ${f.name}`, 10 + Math.round((i / files.length) * 75));

    const before = readMonaco('');
    const beforeSig = before ? before.uri + '|' + before.value.length + '|' + before.value.slice(0, 60) : '';
    const alreadySelected = isSelectedFile(f);

    await activateFile(f, panel);
    const result = await waitForContent(f.name, beforeSig, alreadySelected, panel);

    if (result && result.value != null) {
      collected.push({ name: f.name, text: result.value });
      f.status = 'הורד בהצלחה ✓';
      downloadOne(f.name, result.value);
    } else {
      f.status = 'קריאה נכשלה ✗';
    }
    renderFiles(files);
    await sleep(350);
  }

  setProgress(`הסתיים! הורדו ${collected.length} מתוך ${files.length} קבצים.`, 100);
  barEl.style.background = '#137333';
  isRunning = false;
};

// הוספת הכפתור הצף באופן עמיד (מוודא שהוא לעולם לא נעלם גם ברינדור מחדש של גוגל)
const addLauncherButton = () => {
  if (document.getElementById('gas-downloader-launcher')) return;
  const host = document.body || document.documentElement;
  if (!host) return;

  const btn = E('button', null, 'gas-downloader-launcher', `
    position: fixed !important;
    z-index: 2147483646 !important;
    bottom: 20px !important;
    left: 20px !important;
    height: 40px !important;
    padding: 0 18px 0 14px !important;
    background: #1a73e8 !important;
    color: #ffffff !important;
    border: none !important;
    border-radius: 20px !important;
    box-shadow: 0 1px 3px rgba(60,64,67,0.3), 0 4px 8px 3px rgba(60,64,67,0.15) !important;
    font-family: 'Google Sans', Roboto, Arial, sans-serif !important;
    font-size: 13px !important;
    font-weight: 500 !important;
    cursor: pointer !important;
    display: flex !important;
    align-items: center !important;
    gap: 8px !important;
    direction: rtl !important;
  `);
  btn.type = 'button';
  btn.title = 'הורדת כל קובצי הפרויקט למחשב בלחיצה אחת';

  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute('width', '18');
  svg.setAttribute('height', '18');
  svg.setAttribute('viewBox', '0 0 24 24');
  svg.setAttribute('fill', '#ffffff');
  svg.style.pointerEvents = 'none';

  const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  path.setAttribute('d', 'M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM17 13l-5 5-5-5h3V9h4v4h3z');
  svg.appendChild(path);

  const label = E('span', 'הורדת קוד', null, 'pointer-events: none;');
  btn.append(svg, label);

  btn.onmouseover = () => { btn.style.background = '#1765cc'; };
  btn.onmouseout = () => { btn.style.background = '#1a73e8'; };
  btn.onclick = startProcess;

  host.appendChild(btn);
};

// בדיקה רציפה אחת לשנייה — מבטיח שהכפתור יחזור מיד גם אם גוגל מרנדרת מחדש את הדף
setInterval(addLauncherButton, 1000);
addLauncherButton();

})();
