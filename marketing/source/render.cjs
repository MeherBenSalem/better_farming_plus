// Renders the listing artwork in marketing/images/ from real in-game captures (marketing/images/screenshots/) and the
// bundled OFL fonts. Usage (from the repository root): node marketing/source/render.cjs [name...]
// Requires the `playwright` package with a Chromium build available (NODE_PATH="$(npm root -g)" if installed globally).
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

// The release and the supported Minecraft versions. Adding a version is one line here
// (plus one row in the download table of curseforge/description.md).
const RELEASE = { version: '1.0', name: 'The Self-Running Farm' };
const VERSIONS = [
  { minecraft: '1.20.1', loaders: ['Fabric', 'Forge'] },
  { minecraft: '1.21.1', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.1.2', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.2', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.3', loaders: ['Fabric', 'NeoForge'] },
];
const LOADERS = ['Fabric', 'Forge', 'NeoForge'].filter((loader) => VERSIONS.some((v) => v.loaders.includes(loader)));

// The palette: soil, wheat, leaf, water and crate wood. Each feature has its colour.
const C = { soil: '#1f150c', loam: '#3a2615', wheat: '#f4c64e', cream: '#fff4d6', leaf: '#8fd14f', water: '#5ec8f0', wood: '#d79a55', berry: '#e2574c' };

const root = path.resolve(__dirname, '..', '..');
const out = path.join(root, 'marketing', 'images');
const shots = path.join(out, 'screenshots');
const fonts = path.join(__dirname, 'fonts');
const url = (file) => 'file://' + file;
const shot = (name) => url(path.join(shots, name));

const css = `
@font-face { font-family: Display; font-weight: 700; src: url('${url(path.join(fonts, 'fraunces-700.woff2'))}'); }
@font-face { font-family: Display; font-weight: 900; src: url('${url(path.join(fonts, 'fraunces-900.woff2'))}'); }
@font-face { font-family: Body; font-weight: 700; src: url('${url(path.join(fonts, 'nunito-700.woff2'))}'); }
@font-face { font-family: Body; font-weight: 900; src: url('${url(path.join(fonts, 'nunito-900.woff2'))}'); }
* { box-sizing: border-box; }
html, body { margin: 0; background: ${C.soil}; color: ${C.cream}; font-family: Body, sans-serif; font-weight: 700; }
.stage { position: relative; overflow: hidden;
         background: radial-gradient(ellipse at 85% -10%, rgba(255,214,110,.28) 0%, rgba(255,214,110,0) 45%),
                     linear-gradient(160deg, ${C.loam} 0%, ${C.soil} 70%); }
.display { font-family: Display, serif; font-weight: 900; letter-spacing: -.01em; }
.title { background: linear-gradient(180deg, #fff8e1 0%, #ffe28a 42%, ${C.wheat} 68%, #d9962a 100%);
         -webkit-background-clip: text; background-clip: text; color: transparent; }
.shadow { filter: drop-shadow(0 4px 0 rgba(20,12,6,.85)) drop-shadow(0 0 26px rgba(244,198,78,.25)); }
.kicker { font-weight: 900; color: ${C.leaf}; letter-spacing: .2em; font-size: 22px; text-transform: uppercase; }
.furrows { position: absolute; inset: 0; opacity: .09;
           background: repeating-linear-gradient(115deg, #000 0 14px, transparent 14px 46px); }
.sun { position: absolute; width: 520px; height: 520px; right: -170px; top: -230px; border-radius: 50%;
       background: radial-gradient(circle, rgba(255,226,138,.55) 0%, rgba(255,226,138,.08) 55%, rgba(255,226,138,0) 70%); }
.backdrop { position: absolute; inset: -40px; background-size: cover; background-position: center;
            filter: blur(14px) brightness(.32) saturate(1.2); }
.frame { position: relative; overflow: hidden; background: #000; border-radius: 18px; border: 6px solid ${C.cream};
         box-shadow: 0 0 0 3px rgba(20,12,6,.9), 0 26px 60px rgba(0,0,0,.6); }
.frame > img, .frame > div > img { display: block; }
.label { position: absolute; display: inline-flex; align-items: center; gap: 10px; padding: 7px 16px 6px; border-radius: 999px;
         font-weight: 900; font-size: 18px; letter-spacing: .08em; text-transform: uppercase; color: ${C.soil}; background: ${C.cream};
         box-shadow: 0 6px 18px rgba(0,0,0,.45); }
.dot { display: inline-block; width: 12px; height: 12px; border-radius: 50%; }
.seed { display: inline-block; width: 12px; height: 18px; border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%; }
.row { height: 4px; border-radius: 2px; background: repeating-linear-gradient(90deg, ${C.wheat} 0 18px, transparent 18px 28px); opacity: .85; }
ul.points { list-style: none; padding: 0; margin: 0; }
ul.points li { position: relative; padding-left: 36px; margin: 0 0 15px; font-size: 27px; line-height: 1.3; color: #f3e6c4; }
ul.points li::before { content: ''; position: absolute; left: 4px; top: 8px; width: 14px; height: 20px;
                       border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%; background: var(--accent, ${C.wheat}); }
b.hi { color: #fff; font-weight: 900; }
.stats { display: flex; margin-top: 22px; border-radius: 16px; overflow: hidden; background: rgba(31,21,12,.88);
         border: 3px solid rgba(255,244,214,.25); box-shadow: 0 18px 50px rgba(0,0,0,.5); }
.stats > div { flex: 1; padding: 13px 22px 11px; border-left: 3px solid rgba(255,244,214,.12); }
.stats > div:first-child { border-left: 0; }
.stats .t { font-weight: 900; font-size: 15px; letter-spacing: .16em; color: #c9b48a; text-transform: uppercase; }
.stats .v { font-family: Display, serif; font-weight: 900; font-size: 32px; color: ${C.cream}; margin-top: 2px; white-space: nowrap; }
.pill { display: inline-block; border-radius: 999px; padding: 7px 20px 6px; margin-right: 10px; font-weight: 900; font-size: 21px;
        letter-spacing: .06em; color: ${C.soil}; background: ${C.wheat}; }
.step { display: flex; align-items: center; gap: 16px; padding: 14px 20px; border-radius: 16px; background: rgba(31,21,12,.9);
        border: 3px solid var(--c); box-shadow: 0 14px 34px rgba(0,0,0,.45); }
.step .n { font-family: Display, serif; font-weight: 900; font-size: 40px; color: var(--c); line-height: 1; }
.step .s { font-size: 22px; line-height: 1.2; color: ${C.cream}; }
`;

const page = (width, height, body, transparent = false) => `<!doctype html><html><head><meta charset="utf-8"><style>${css}
body { width: ${width}px; height: ${height}px; } .stage { width: ${width}px; height: ${height}px; }
${transparent ? 'html, body, .stage { background: transparent; }' : ''}</style></head>
<body><div class="stage">${body}</div></body></html>`;

// An exact region (x, y, w, h in 1920x1080 pixels) of a capture, scaled to W pixels wide.
const region = (file, x, y, w, h, W) => `<div style="position:relative;width:${W}px;height:${Math.round(W * h / w)}px;overflow:hidden">
    <img src="${shot(file)}" style="position:absolute;max-width:none;width:${W * 1920 / w}px;left:${-x * W / w}px;top:${-y * W / w}px"></div>`;
const framed = (inner, style = '') => `<div class="frame" style="${style}">${inner}</div>`;
const label = (text, color, pos) => `<span class="label" style="${pos}"><span class="dot" style="background:${color}"></span>${text}</span>`;
const stats = (items) => `<div class="stats">${items.map(([t, v, flex]) =>
  `<div${flex ? ` style="flex:${flex}"` : ''}><div class="t">${t}</div><div class="v">${v}</div></div>`).join('')}</div>`;

// A feature card: copy on the left, artwork on the right.
const card = ({ kicker, title, points, art, width = 1600, height = 900, artWidth = 940, backdrop = 'farm.jpg', color = C.wheat }) => page(width, height, `
  <div class="backdrop" style="background-image:url('${shot(backdrop)}')"></div><div class="furrows"></div><div class="sun"></div>
  <div style="position:absolute;left:78px;top:0;bottom:0;width:${width - artWidth - 150}px;display:flex;flex-direction:column;justify-content:center;--accent:${color}">
    <div class="kicker" style="color:${color}">${kicker}</div>
    <div class="display title shadow" style="font-size:68px;line-height:1.02;margin:14px 0 22px">${title}</div>
    <div class="row" style="width:220px;margin-bottom:30px;background:repeating-linear-gradient(90deg, ${color} 0 18px, transparent 18px 28px)"></div>
    <ul class="points">${points.map((point) => `<li>${point.endsWith('>') ? point : point.replace(/ (\S+)$/, '&nbsp;$1')}</li>`).join('')}</ul>
  </div>
  <div style="position:absolute;right:64px;top:50%;transform:translateY(-50%);width:${artWidth}px">${art}</div>`);

const versionText = VERSIONS.map((v) => v.minecraft).join(' · ');

// The first lines of the config file the mod writes on first start (copied from a generated file).
const configPanel = () => {
  const file = path.join(__dirname, 'config-excerpt.json');
  const esc = (t) => t.replace(/&/g, '&amp;').replace(/</g, '&lt;');
  const text = fs.readFileSync(file, 'utf8').split('\n').slice(0, 26).map((line) => {
    if (/^\s*\/\//.test(line)) return `<span style="color:#9c8a68">${esc(line)}</span>`;
    return esc(line).replace(/("[^"]+")(:)/g, '<span style="color:#ffe28a">$1</span>$2')
      .replace(/: (true|false|-?[\d.]+|"[^"]*")/g, ': <span style="color:#a6e07a">$1</span>');
  }).join('\n');
  return `<pre style="margin:0;padding:20px 22px;font-size:13.6px;line-height:1.52;color:#eadfca;white-space:pre;font-family:'DejaVu Sans Mono',monospace">${text}</pre>`;
};

// Three steps of the farm loop.
const loopSteps = () => `<div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:16px;margin-top:22px">
  ${[[C.water, '1', 'Sprinkler waters<br>and grows'], [C.wood, '2', 'Crate harvests<br>and replants'], [C.leaf, '3', 'Hopper carries<br>it away']]
    .map(([c, n, s]) => `<div class="step" style="--c:${c}"><div class="n">${n}</div><div class="s">${s}</div></div>`).join('')}</div>`;

const header = (title, color = C.wheat) => page(1400, 120, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:24px">
    <div class="row" style="width:240px"></div>
    <span class="seed" style="background:${color}"></span>
    <div class="display title shadow" style="font-size:58px;white-space:nowrap">${title}</div>
    <span class="seed" style="background:${color}"></span>
    <div class="row" style="width:240px"></div>
  </div>`, true);

const divider = () => page(1400, 44, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:16px">
    <div class="row" style="width:420px;opacity:.7"></div>
    ${[C.leaf, C.wheat, C.water, C.wood, C.berry].map((c) => `<span class="seed" style="background:${c}"></span>`).join('')}
    <div class="row" style="width:420px;opacity:.7"></div>
  </div>`, true);

const assets = {
  banner: () => page(1800, 600, `
    <img src="${shot('farm.jpg')}" style="position:absolute;right:-120px;top:-120px;width:1320px;-webkit-mask-image:linear-gradient(90deg,transparent 0%,#000 30%);mask-image:linear-gradient(90deg,transparent 0%,#000 30%)">
    <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(31,21,12,.98) 0%,rgba(31,21,12,.92) 34%,rgba(31,21,12,.35) 60%,rgba(31,21,12,0) 82%)"></div>
    <div style="position:absolute;inset:0;background:linear-gradient(0deg,rgba(31,21,12,.75) 0%,rgba(31,21,12,0) 30%)"></div>
    <div class="furrows" style="-webkit-mask-image:linear-gradient(90deg,#000 0%,#000 36%,transparent 58%);mask-image:linear-gradient(90deg,#000 0%,#000 36%,transparent 58%)"></div>
    <div class="sun" style="right:auto;left:420px;top:-300px;opacity:.7"></div>
    <div style="position:absolute;left:84px;top:64px;width:1100px">
      <div class="kicker">${RELEASE.version} · ${RELEASE.name}</div>
      <div class="display title shadow" style="display:inline-block;font-size:110px;line-height:1;margin:14px 0 20px;padding-right:12px;white-space:nowrap">Better Farming++</div>
      <div class="row" style="width:560px;margin-bottom:24px"></div>
      <div style="font-size:33px;color:${C.cream};line-height:1.3;font-weight:900">Sprinklers that grow your field, a crate that harvests<br>and replants it, and hoes that sweep a whole row.</div>
      <div style="margin-top:30px">${LOADERS.map((l) => `<span class="pill">${l}</span>`).join('')}
        <span style="font-size:22px;color:#d8c69c;margin-left:6px;font-weight:900">${versionText}</span></div>
    </div>`),

  icon: () => page(512, 512, `
    <div style="position:absolute;inset:0;border-radius:96px;overflow:hidden;background:${C.soil}">
      <img src="${shot('crops.jpg')}" style="position:absolute;width:1640px;left:-600px;top:-330px;filter:saturate(1.2) contrast(1.05)">
      <div style="position:absolute;inset:0;background:radial-gradient(ellipse at 50% 46%,rgba(31,21,12,.15) 0%,rgba(31,21,12,.88) 78%)"></div>
      <div style="position:absolute;left:96px;top:96px;width:320px;height:320px;border-radius:50%;background:radial-gradient(circle at 38% 32%,#b6ec7a,${C.leaf} 55%,#4f8a26);
                  box-shadow:0 0 0 12px rgba(31,21,12,.85),0 0 0 20px ${C.wheat},0 20px 50px rgba(0,0,0,.6)"></div>
      <div class="display" style="position:absolute;left:0;right:0;top:142px;text-align:center;font-size:200px;line-height:1;color:${C.cream};
                  text-shadow:0 8px 0 #2f5214, 0 0 30px rgba(255,244,214,.35)">++</div>
    </div>
    <div style="position:absolute;inset:0;border-radius:96px;border:10px solid ${C.wheat};box-shadow:inset 0 0 0 5px ${C.soil}"></div>`, true),

  'card-loop': () => card({
    kicker: 'The headline', title: 'A farm that<br>runs itself', color: C.leaf,
    points: ['Plant once: a <b class="hi">Sprinkler</b> keeps the field wet and growing', 'A <b class="hi">Farming Crate</b> harvests every ripe crop and replants it',
      'A hopper under the crate carries the harvest away', 'No redstone, no water channels through the field'],
    art: `${framed(region('farm.jpg', 120, 40, 1680, 945, 940))}${loopSteps()}` }),

  'card-sprinkler': () => card({
    kicker: 'New block', title: 'The<br>Sprinkler', color: C.water, backdrop: 'sprinkler.jpg',
    points: ['Hide one water block up to 4 blocks below it, or set it beside water', 'Keeps every farmland block in a 9×9 square wet',
      'Gives growing plants extra growth ticks: about 3× as many as vanilla', 'Each plant grows by its own rules: vanilla crops, and modded ones in <b class="hi">#minecraft:crops</b>'],
    art: `<div style="position:relative">${framed(region('sprinkler.jpg', 160, 60, 1600, 900, 940), `border-color:${C.water}`)}
      <div class="frame" style="position:absolute;right:-26px;bottom:-60px;width:400px;border-color:${C.water}">${region('cutaway.jpg', 240, 135, 1440, 810, 400)}</div>
      ${label('Cutaway: the water under the field', C.water, 'right:-6px;bottom:194px;font-size:14px')}</div>
      <div style="margin-top:74px;width:520px">${stats([['Area', '9×9'], ['Every', '5 s'], ['Growth', '≈3×']])}</div>` }),

  'card-crate': () => card({
    kicker: 'Rebuilt', title: 'The Farming<br>Crate', color: C.wood, backdrop: 'crate.jpg',
    points: ['Harvests every ripe crop in the 9×9 around it, once a second', 'Replants each one with a seed from its own drops',
      'Stores every drop; when it is full it leaves the field standing: <b class="hi">nothing is ever lost</b>',
      'Hoppers take from it on every side, comparators read it'],
    art: `<div style="position:relative">${framed(region('crate.jpg', 160, 60, 1600, 900, 940), `border-color:${C.wood}`)}
      <div class="frame" style="position:absolute;right:-26px;bottom:-70px;width:420px;border-color:${C.wood}">${region('crate_gui.jpg', 560, 180, 800, 720, 420)}</div></div>
      <div style="margin-top:84px;width:500px">${stats([['Area', '9×9'], ['Every', '1 s'], ['Lost', '0']])}</div>` }),

  'card-sweep': () => card({
    kicker: 'Hoes matter', title: 'One swing,<br>a whole patch', color: C.wheat, backdrop: 'sweep_after.jpg',
    points: ['Iron and golden hoes harvest <b class="hi">3×3</b>, diamond <b class="hi">5×5</b>, netherite <b class="hi">7×7</b>',
      'Only ripe crops are taken; young ones stay', 'Sneak to take a single crop', 'Fortune counts, and the hoe wears 1 per crop'],
    art: `<div style="display:grid;grid-template-columns:1fr 1fr;gap:18px">
      <div style="position:relative">${framed(region('sweep_before.jpg', 470, 230, 980, 735, 461))}${label('Before', C.wheat, 'left:16px;top:16px')}</div>
      <div style="position:relative">${framed(region('sweep_after.jpg', 470, 230, 980, 735, 461))}${label('One click', C.leaf, 'left:16px;top:16px')}</div></div>
      ${stats([['Iron · Gold', '3×3'], ['Diamond', '5×5'], ['Netherite', '7×7']])}` }),

  'card-harvest': () => card({
    kicker: 'Right-click harvest', title: 'Every crop,<br>replanted', color: C.leaf, backdrop: 'crops.jpg',
    points: ['Wheat, carrots, potatoes, beetroots, <b class="hi">nether wart</b> and <b class="hi">cocoa</b>',
      'Modded crops too, and anything in a block tag', 'Replants with one seed from the drops, never for free',
      'Never touches what stands around the crop', 'Empty hand or any hoe'],
    art: framed(region('crops.jpg', 160, 60, 1600, 900, 940)) }),

  'card-indicators': () => card({
    kicker: 'See what is ready', title: 'Harvest<br>indicators', color: C.berry, backdrop: 'indicators.jpg',
    points: ['A marker floats above every ripe crop', 'Wheat, carrots, potatoes, beetroots, nether wart, sweet berries and stems',
      'A built-in resource pack, on by default', 'Turn it off or reorder it in the Resource Packs screen'],
    art: framed(region('indicators.jpg', 160, 60, 1600, 900, 940)) }),

  'card-config': () => card({
    kicker: 'For servers and modpacks', title: 'No more<br>trampling', color: C.leaf, backdrop: 'farm.jpg',
    points: ['Nobody, player or mob, tramples farmland (or keep vanilla rules)',
      '<b class="hi">config/better_farming_plus.json</b> with comments; 0.5.0 settings migrate',
      'Sweep sizes per hoe or item tag, areas, timings and growth chance', 'No extra library to install'],
    art: `<div style="display:flex;flex-direction:column;gap:18px">
      ${framed(configPanel(), `background:#1b120a;border-color:${C.leaf}`)}
      ${stats([['Loaders', 'Fabric · NeoForge · Forge', 2], ['Versions', '5']])}</div>` }),
};

const HEADERS = {
  'header-whats-new': ['What’s New in 1.0', C.leaf], 'header-loop': ['A Farm That Runs Itself', C.leaf],
  'header-sprinkler': ['The Sprinkler', C.water], 'header-crate': ['The Farming Crate', C.wood],
  'header-harvest': ['Harvesting', C.wheat], 'header-indicators': ['Harvest Indicators', C.berry],
  'header-config': ['For Servers and Modpacks', C.leaf], 'header-downloads': ['Downloads', C.wheat],
  'header-faq': ['FAQ', C.wheat], 'header-gallery': ['Gallery', C.wheat],
};
for (const [name, [title, color]] of Object.entries(HEADERS)) {
  assets[name] = () => header(title, color);
}
assets.divider = divider;

(async () => {
  const wanted = process.argv.slice(2);
  const names = wanted.length ? wanted : Object.keys(assets);
  const browser = await chromium.launch();
  const context = await browser.newContext({ deviceScaleFactor: 1 });
  for (const name of names) {
    if (!assets[name]) {
      console.error('unknown asset', name);
      continue;
    }
    const html = assets[name]();
    const file = path.join(__dirname, `.render-${name}.html`);
    fs.writeFileSync(file, html);
    const p = await context.newPage();
    await p.goto(url(file));
    await p.evaluate(() => document.fonts.ready);
    await p.waitForTimeout(200);
    const { width, height } = await p.evaluate(() => ({ width: document.body.scrollWidth, height: document.body.scrollHeight }));
    await p.setViewportSize({ width, height });
    const transparent = name.startsWith('header') || name === 'divider' || name === 'icon';
    await p.screenshot({ path: path.join(out, `${name}.png`), omitBackground: transparent });
    await p.close();
    fs.unlinkSync(file);
    console.log('rendered', name);
  }
  await browser.close();
})();
