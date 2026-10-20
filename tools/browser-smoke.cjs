/** Real Chromium input smoke test. npm install --no-save --package-lock=false playwright */
const {chromium}=require('playwright');
const assert=require('node:assert/strict');
let activeBrowser;
(async()=>{
 const browser=activeBrowser=await chromium.launch({executablePath:process.env.CHROMIUM||'/usr/bin/chromium',headless:!process.env.DISPLAY,args:['--no-sandbox','--enable-unsafe-swiftshader','--use-angle=swiftshader']});
 const context=await browser.newContext({viewport:{width:1280,height:720},reducedMotion:'reduce'});
 const page=await context.newPage();
 // Apply deliberate test-save fixtures after the outgoing page has completed its lifecycle save.
 await page.addInitScript(()=>{const raw=sessionStorage.getItem('nebula-test-fixture');if(raw){for(const [key,value] of Object.entries(JSON.parse(raw)))localStorage.setItem(key,value);sessionStorage.removeItem('nebula-test-fixture');}});
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 const snap=()=>page.evaluate(()=>window.nebulaSnapshot);
 const waitState=state=>page.waitForFunction(s=>window.nebulaSnapshot?.state===s,state);
 const click=async(x,y)=>{await page.mouse.click(x,720-y);await page.waitForTimeout(80);};
 await page.goto(process.env.GAME_URL||'http://127.0.0.1:8000');await waitState('TITLE');
 assert.equal((await snap()).reduced,true);await page.screenshot({path:'artifacts/title.png'});
 await click(200,220);await waitState('READY');await click(640,175);await waitState('MINING');
 for(let i=0;i<5;i++){
  const before=await snap();await page.keyboard.type(before.target.toUpperCase(),{delay:12});
  await page.waitForFunction(n=>window.nebulaSnapshot.words>n,before.words);
 }
 assert.equal((await snap()).combo,5);await page.keyboard.type('#');await page.waitForTimeout(100);assert.equal((await snap()).combo,0);assert.equal((await snap()).misses,1);
 await page.screenshot({path:'artifacts/mining.png'});
 await page.keyboard.press('Escape');await waitState('PAUSED');const paused=(await snap()).elapsed;await page.waitForTimeout(700);assert.equal((await snap()).elapsed,paused);
 await click(640,220);await waitState('MINING');
 // Playwright keeps pages focused, so inject the browser blur event to exercise the real lifecycle listener.
 // Native user tab switching remains a manual check; this does not claim an OS focus transition.
 await page.evaluate(()=>window.dispatchEvent(new Event('blur')));
 await waitState('PAUSED');const blurred=(await snap()).elapsed;
 await page.waitForTimeout(300);assert.equal((await snap()).elapsed,blurred);
 await click(640,220);await waitState('MINING');
 // Keep the actual sixty-second game loop running; no test-only state mutation.
 const start=Date.now();
 while((await snap()).state==='MINING' && Date.now()-start<70000){
  const s=await snap();await page.keyboard.type(s.target,{delay:7});await page.waitForTimeout(100);
 }
 await waitState('RESULT');const result=await snap();assert.equal(result.elapsed,60);assert.ok(result.words>=20);assert.ok(result.ore>=700);assert.ok(result.rare>=4);
 await page.screenshot({path:'artifacts/result.png'});
 await click(800,150);await waitState('BASE');
 const beforeRobot=await snap();await click(930,386);assert.equal((await snap()).robots,1);assert.ok((await snap()).ore<beforeRobot.ore);
 const beforeLaser=await snap();await click(930,227);assert.equal((await snap()).laser,1);assert.ok((await snap()).ore<beforeLaser.ore);
 await click(930,85);assert.equal((await snap()).planet,'MARS');
 const beforeProduction=(await snap()).ore;await page.waitForTimeout(1300);assert.ok((await snap()).ore>=beforeProduction+1);
 await page.screenshot({path:'artifacts/base.png'});
 // Settings and actual browser persistence.
 await click(130,100);await waitState('SETTINGS');await click(340,400);assert.equal((await snap()).mute,true);
 await click(500,325);assert.equal((await snap()).reduced,false);await click(790,145);await waitState('BASE');
 const saved=await snap();await page.reload();await waitState('TITLE');assert.equal((await snap()).robots,1);assert.equal((await snap()).laser,1);assert.equal((await snap()).planet,'MARS');assert.equal((await snap()).mute,true);assert.ok((await snap()).ore>=saved.ore);
 await click(500,220);await waitState('BASE');
 await page.setViewportSize({width:960,height:600});await page.waitForTimeout(200);await page.screenshot({path:'artifacts/resized.png'});assert.equal((await snap()).state,'BASE');
 await page.setViewportSize({width:640,height:360});await page.waitForTimeout(150);await page.screenshot({path:'artifacts/compact.png'});
 await page.setViewportSize({width:1280,height:720});
 // Explicit save fixtures test offline clamping; they are not used for gameplay checks above.
 await page.evaluate(()=>sessionStorage.setItem('nebula-test-fixture',JSON.stringify({'nebula-miner-v1:robotss':'2','nebula-miner-v1:ores':'0','nebula-miner-v1:savedAts':String(Date.now()-5*3600000)})));
 await page.reload();await waitState('TITLE');assert.equal((await snap()).robots,2);assert.ok((await snap()).ore>=28800&&(await snap()).ore<28802);
 await click(320,100);await waitState('SETTINGS');await click(420,145);assert.equal((await snap()).robots,2);await click(420,145);await waitState('TITLE');assert.equal((await snap()).robots,0);assert.equal((await snap()).ore,0);
 await page.evaluate(()=>sessionStorage.setItem('nebula-test-fixture',JSON.stringify({'nebula-miner-v1:versions':'1','nebula-miner-v1:ores':'NaN','nebula-miner-v1:robotss':'Infinity','nebula-miner-v1:rares':'-9'})));
 await page.reload();await waitState('TITLE');assert.equal((await snap()).ore,0);assert.equal((await snap()).robots,0);assert.equal((await snap()).rare,0);
 assert.deepEqual(errors,[]);
 console.log(JSON.stringify({status:'passed',result,persistence:'verified',focusPause:'verified via injected blur event',offlineCap:'verified with fixture',corruptSave:'verified with fixture',resetConfirmation:'verified',screenshots:['title','mining','result','base','resized'],pageErrors:errors},null,2));
 await browser.close();
})().catch(async e=>{console.error(e);if(activeBrowser)await activeBrowser.close();process.exitCode=1;});
