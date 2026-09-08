// ==UserScript==
// @name         Google Apps Script - הורדת קבצי הפרויקט
// @namespace    https://mitmachim.top/user/לומדעס
// @version      1.0.0
// @description  הורדת קבצי GS, HTML והמניפסט ישירות מעורך Google Apps Script
// @author       לומדעס
// @match        https://script.google.com/home/projects/*/edit*
// @run-at       document-idle
// @grant        none
// @inject-into  page
// ==/UserScript==

(()=>{
'use strict';
const openDownloader=()=>{
if(document.getElementById('gas-downloader-debug')){document.getElementById('gas-downloader-debug').remove();return}
const sleep=ms=>new Promise(r=>setTimeout(r,ms));
const clean=s=>String(s||'').replace(/[\u200e\u200f]/g,'').trim();
const safe=s=>clean(s).replace(/[\\/:*?"<>|]+/g,'_').replace(/\s+/g,' ').slice(0,120)||'code.gs';
const panel=document.createElement('section');
panel.id='gas-downloader-debug';panel.dir='rtl';
panel.style='position:fixed;z-index:2147483647;top:12px;left:12px;width:430px;max-width:calc(100vw - 24px);max-height:calc(100vh - 24px);display:flex;flex-direction:column;background:#fff;color:#202124;border:2px solid #1a73e8;border-radius:12px;box-shadow:0 10px 35px #0006;font:13px Arial,sans-serif;text-align:right';
const E=(tag,text,id,style)=>{const e=document.createElement(tag);if(text!=null)e.textContent=text;if(id)e.id=id;if(style)e.style.cssText=style;return e};
const header=E('div',null,null,'display:flex;align-items:center;gap:8px;padding:12px 14px;border-bottom:1px solid #ddd');
header.append(E('b','מוריד Apps Script — מעקב',null,'font-size:16px;flex:1'),E('button','העתק לוג','gadCopy'),E('button','✕','gadClose'));
const top=E('div',null,null,'padding:10px 14px'),stateEl=E('div','הסקריפט הופעל בהצלחה. מתחיל סריקה…','gadState');
const track=E('div',null,null,'height:7px;background:#e8eaed;border-radius:5px;overflow:hidden;margin-top:8px'),barEl=E('div',null,'gadBar','height:100%;width:1%;background:#1a73e8;transition:width .2s');
track.append(barEl);top.append(stateEl,track);
const filesEl=E('div',null,'gadFiles','padding:0 14px 8px'),logEl=E('pre',null,'gadLog','direction:ltr;text-align:left;white-space:pre-wrap;overflow:auto;min-height:120px;max-height:280px;margin:0;padding:10px 14px;background:#202124;color:#d7f8d7;font:12px Consolas,monospace');logEl.dir='ltr';
const footer=E('div',null,null,'display:flex;gap:8px;padding:10px 14px'),retryEl=E('button','סרוק שוב','gadRetry','flex:1'),downloadEl=E('button','הורד קבצים שנקראו','gadDownload','flex:1;background:#1a73e8;color:#fff;border:0;border-radius:5px');downloadEl.disabled=true;footer.append(retryEl,downloadEl);panel.append(header,top,filesEl,logEl,footer);
document.body.appendChild(panel);
const q=id=>panel.querySelector('#'+id),logBox=q('gadLog'),state=q('gadState'),bar=q('gadBar'),filesBox=q('gadFiles'),downloadBtn=q('gadDownload');
const logs=[];const log=(message,type='INFO')=>{const line=`${new Date().toLocaleTimeString('he-IL')} [${type}] ${message}`;logs.push(line);logBox.textContent=logs.join('\n');logBox.scrollTop=logBox.scrollHeight;console.log('[GAS Downloader]',message)};
const progress=(message,value)=>{state.textContent=message;bar.style.width=Math.max(1,Math.min(100,value))+'%'};
q('gadClose').onclick=()=>panel.remove();
q('gadCopy').onclick=async()=>{try{await navigator.clipboard.writeText(logs.join('\n'));log('הלוג הועתק ללוח')}catch(e){log('העתקת הלוג נכשלה: '+e.message,'ERROR')}};
const visible=el=>{const r=el.getBoundingClientRect(),s=getComputedStyle(el);return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden'};
const extractName=el=>{const raw=clean(el.getAttribute('aria-label')||el.getAttribute('title')||el.innerText||el.textContent);const matches=[...raw.matchAll(/([^\n]+?\.(?:gs|html|json))(?=\n|$)/ig)];return matches.length===1?safe(matches[0][1]):''};
const discoverFiles=()=>{const result=new Map();for(const el of document.querySelectorAll('*')){if(panel.contains(el)||!visible(el))continue;const name=extractName(el);if(!name)continue;const r=el.getBoundingClientRect(),score=r.width*r.height;const old=result.get(name);if(!old||score<old.score)result.set(name,{el,score})}return[...result].map(([name,x])=>({name,el:x.el,status:'ממתין'}))};
const monacoContexts=()=>{const found=[];const visit=w=>{try{if(w.monaco&&w.monaco.editor)found.push(w);for(let i=0;i<w.frames.length;i++)visit(w.frames[i])}catch(_){}};visit(window);return found};
const readMonaco=(wanted)=>{for(const w of monacoContexts()){const api=w.monaco.editor;const editors=typeof api.getEditors==='function'?api.getEditors():[];const candidates=[...editors].sort((a,b)=>{try{return Number(b.hasTextFocus())-Number(a.hasTextFocus())}catch(_){return 0}});for(const ed of candidates){try{const model=ed.getModel();if(!model)continue;const uri=String(model.uri||'');if(uri.toLowerCase().includes(wanted.toLowerCase().replace(/\.(gs|html)$/i,''))||ed.hasTextFocus()||(ed.getDomNode()&&visible(ed.getDomNode())))return{value:model.getValue(),source:'Monaco editor',uri}}catch(_){}}const models=typeof api.getModels==='function'?api.getModels():[];for(const model of models){try{const uri=String(model.uri||'');if(uri.toLowerCase().includes(wanted.toLowerCase().replace(/\.(gs|html)$/i,'')))return{value:model.getValue(),source:'Monaco model',uri}}catch(_){}}}return null};
const editorSnapshot=()=>{const r=readMonaco('');return r?{value:r.value,uri:r.uri,signature:r.uri+'|'+r.value.length+'|'+r.value.slice(0,80)+'|'+r.value.slice(-80)}:null};
const isSelectedFile=f=>{let e=f.el;for(let i=0;e&&i<6;i++,e=e.parentElement){const cls=String(e.className||'').toLowerCase();if(e.getAttribute('aria-selected')==='true'||e.getAttribute('aria-current')==='true'||/(^|[\s_-])(selected|active)([\s_-]|$)/.test(cls))return true}return false};
const activateFile=async f=>{const r=f.el.getBoundingClientRect(),x=Math.max(0,Math.min(innerWidth-1,r.left+r.width/2)),y=Math.max(0,Math.min(innerHeight-1,r.top+r.height/2)),hit=document.elementFromPoint(x,y)||f.el,candidates=[];for(const e of [hit,f.el,hit.parentElement,f.el.parentElement,hit.closest('[role="treeitem"]'),hit.closest('[role="button"]'),hit.closest('[tabindex]')])if(e&&!panel.contains(e)&&!candidates.includes(e))candidates.push(e);log(`מפעיל את ${f.name}; ${candidates.length} יעדי לחיצה אפשריים`);for(const e of candidates){try{e.scrollIntoView({block:'nearest'});e.focus?.();for(const type of ['pointerdown','mousedown','pointerup','mouseup','click'])e.dispatchEvent(new MouseEvent(type,{view:window,bubbles:true,cancelable:true,clientX:x,clientY:y,button:0}));e.click?.();await sleep(180)}catch(_){}}};
const readClipboard=async()=>{const area=[...document.querySelectorAll('.monaco-editor textarea,textarea.inputarea,[role="textbox"]')].find(el=>!panel.contains(el)&&visible(el));if(!area)return null;area.focus();area.dispatchEvent(new KeyboardEvent('keydown',{key:'a',code:'KeyA',ctrlKey:true,bubbles:true,cancelable:true}));area.dispatchEvent(new KeyboardEvent('keyup',{key:'a',code:'KeyA',ctrlKey:true,bubbles:true,cancelable:true}));await sleep(100);try{document.execCommand('copy');await sleep(100);const value=await navigator.clipboard.readText();return value==null?null:{value,source:'Clipboard fallback',uri:''}}catch(e){log('גישה ללוח לא זמינה: '+e.message,'WARN');return null}};
const waitForContent=async(name,before,allowSame)=>{let last=null,stable=0;for(let attempt=1;attempt<=25;attempt++){await sleep(attempt===1?450:220);const current=readMonaco(name)||await readClipboard();if(current){const sig=current.uri+'|'+current.value.length+'|'+current.value.slice(0,80)+'|'+current.value.slice(-80),changed=!before||sig!==before.signature;if(changed||allowSame){if(last&&current.value===last.value&&current.uri===last.uri)stable++;else stable=0;last=current;if(stable>=2)return current}}if(attempt===1||attempt%5===0)log(`ממתין להחלפת העורך עבור ${name}; ניסיון ${attempt}/25`)}return last};
let collected=[];
const renderFiles=files=>{filesBox.replaceChildren(E('b','קבצים שזוהו:'));for(const f of files){const row=E('div',null,null,'display:flex;gap:8px'),name=E('span',f.name,null,'flex:1');name.dir='ltr';row.append(name,E('span',f.status));filesBox.append(row)}};
const downloadOne=(name,text)=>{const blob=new Blob([text],{type:'text/plain;charset=utf-8'}),url=URL.createObjectURL(blob),a=document.createElement('a');a.href=url;a.download=name;document.body.appendChild(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(url),15000)};
downloadBtn.onclick=async()=>{if(!collected.length)return;log(`מתחיל הורדה של ${collected.length} קבצים`);for(let i=0;i<collected.length;i++){downloadOne(collected[i].name,collected[i].text);await sleep(350)}log('פקודות ההורדה נשלחו. אם Chrome שואל על הורדות מרובות — יש לאשר.');progress('ההורדה הסתיימה',100)};
let running=false;
const run=async()=>{if(running)return;running=true;collected=[];downloadBtn.disabled=true;progress('סורק את רשימת הקבצים…',5);log('כתובת הדף: '+location.href);log('כותרת הדף: '+document.title);log('Monaco גלוי: '+(monacoContexts().length?'כן':'לא'),'CHECK');const files=discoverFiles();renderFiles(files);log(`נמצאו ${files.length} שמות קבצים: ${files.map(f=>f.name).join(', ')||'אין'}`);if(!files.length){progress('לא נמצאו קבצים. פתח את אזור "קבצים" ולחץ "סרוק שוב".',0);log('הסריקה לא מצאה רכיבי קבצים גלויים','ERROR');running=false;return}for(let i=0;i<files.length;i++){const f=files[i];f.status='מחליף קובץ…';renderFiles(files);progress(`קורא ${i+1}/${files.length}: ${f.name}`,10+Math.round(i/files.length*75));const before=editorSnapshot(),alreadySelected=isSelectedFile(f);log(`${f.name}: היה פעיל לפני הלחיצה: ${alreadySelected?'כן':'לא'}`);try{await activateFile(f)}catch(e){log('הלחיצה נכשלה: '+e.message,'ERROR')}const result=await waitForContent(f.name,before,alreadySelected);if(result){const duplicate=collected.find(x=>x.text===result.value);if(duplicate){f.status='כפילות — לא נשמר';log(`${f.name}: התקבל אותו תוכן כמו ${duplicate.name}; הקובץ לא נשמר כדי למנוע כפילות`,'ERROR')}else{collected.push({name:f.name,text:result.value});f.status=`נקרא (${result.value.length} תווים)`;log(`${f.name}: נקרא לאחר החלפת העורך; ${result.value.length} תווים${result.uri?'; model='+result.uri:''}`)}}else{f.status='נכשל בהחלפה';log(`${f.name}: העורך לא התחלף ולכן התוכן לא נשמר`,'ERROR')}renderFiles(files)}downloadBtn.disabled=!collected.length;progress(`נקראו ${collected.length} מתוך ${files.length}. לחץ על כפתור ההורדה.`,90);log(`הסריקה הסתיימה: ${collected.length}/${files.length} קבצים מוכנים`);running=false};
q('gadRetry').onclick=run;
log('חלונית המעקב נפתחה — סקריפט Tampermonkey פועל');
run();
};
const addLauncher=()=>{
  if(document.getElementById('gas-downloader-launcher'))return;
  const button=document.createElement('button');
  button.id='gas-downloader-launcher';
  button.type='button';
  button.textContent='הורדת קוד';
  button.title='הורדת כל קובצי הפרויקט';
  button.style.cssText='position:fixed;z-index:2147483646;bottom:18px;left:18px;padding:10px 16px;background:#1a73e8;color:#fff;border:0;border-radius:22px;box-shadow:0 4px 14px #0005;font:600 14px Arial;cursor:pointer';
  button.addEventListener('click',openDownloader);
  document.body.appendChild(button);
};
if(document.body)addLauncher();else addEventListener('DOMContentLoaded',addLauncher,{once:true});
})()
