const T = THREE
              , $ = id => document.getElementById(id);
            const G = 32.2
              , R = .28
              , F = 6
              , PH = 3
              , LX = 1.25
              , HW = .8
              , CX = 2.1
              , FL = .12
              , TOP = 1.1
              , MAXT = .55
              , THR = .44
              , WT = {
                N: .11,
                P: .055
            }
              , TS = .33;
            const view = $('view');
            let scene, cam, ren, pipCam, robot, barrel, roller, beam, traj, seed = 7;
            const flaps = []
              , flapA = [0, 0]
              , balls = []
              , tags = []
              , keys = {};
            const S = {
                x: 0,
                z: 4.6,
                h: 0,
                v: 0,
                pitch: 50,
                power: 20,
                inv: [],
                score: 0,
                tips: 0,
                btn: false,
                th: .55,
                om: 0,
                rest: 1,
                tip: null,
                cnt: [{
                    n: 0,
                    p: 0,
                    w: 0
                }, {
                    n: 0,
                    p: 0,
                    w: 0
                }],
                cam: {
                    m: 0,
                    az: 0,
                    el: .75,
                    d: 17
                }
            };
            const rnd = () => (seed = (seed * 16807) % 2147483647) / 2147483647;
            const log = m => {
                const l = $('log')
                  , d = document.createElement('div');
                d.textContent = m;
                l.appendChild(d);
                while (l.children.length > 40)
                    l.firstChild.remove();
                l.scrollTop = 1e9
            }
            ;
            const std = (c, m=.2, r=.5) => new T.MeshStandardMaterial({
                color: c,
                metalness: m,
                roughness: r
            });
            function box(w, h, l, mat, x, y, z, par, sh=true) {
                const m = new T.Mesh(new T.BoxGeometry(w,h,l),mat);
                m.position.set(x, y, z);
                m.castShadow = sh;
                m.receiveShadow = true;
                (par || scene).add(m);
                return m
            }
            function tex(w, h, fn) {
                const c = document.createElement('canvas');
                c.width = w;
                c.height = h;
                fn(c.getContext('2d'), w, h);
                const t = new T.CanvasTexture(c);
                t.encoding = T.sRGBEncoding;
                t.anisotropy = 8;
                return t
            }

            function build() {
                scene = new T.Scene();
                scene.background = new T.Color(0x44464a);
                scene.fog = new T.Fog(0x44464a,28,70);
                ren = new T.WebGLRenderer({
                    antialias: true
                });
                ren.setPixelRatio(Math.min(devicePixelRatio, 2));
                ren.outputEncoding = T.sRGBEncoding;
                ren.toneMapping = T.ACESFilmicToneMapping;
                ren.toneMappingExposure = .9;
                ren.shadowMap.enabled = true;
                ren.shadowMap.type = T.PCFSoftShadowMap;
                view.prepend(ren.domElement);
                cam = new T.PerspectiveCamera(48,1,.1,120);
                scene.add(new T.HemisphereLight(0xbcd0ff,0x2b2b33,.75));
                const sun = new T.DirectionalLight(0xfff1dd,1.25);
                sun.position.set(8, 18, 10);
                sun.castShadow = true;
                sun.shadow.mapSize.set(2048, 2048);
                Object.assign(sun.shadow.camera, {
                    left: -9,
                    right: 9,
                    top: 9,
                    bottom: -9,
                    near: 1,
                    far: 50
                });
                sun.shadow.bias = -.0004;
                scene.add(sun);
                const rim = new T.DirectionalLight(0x6d8cff,.4);
                rim.position.set(-10, 8, -10);
                scene.add(rim);
                // piso
                const out = new T.Mesh(new T.PlaneGeometry(120,120),std(0x55575b, 0, 1));
                out.rotation.x = -Math.PI / 2;
                out.position.y = -.02;
                out.receiveShadow = true;
                scene.add(out);
                const ft = tex(1024, 1024, (g, w, h) => {
                    const u = w / 12;
                    for (let i = 0; i < 12; i++)
                        for (let j = 0; j < 12; j++) {
                            g.fillStyle = (i + j) % 2 ? '#b9cad6' : '#b2c4d1';
                            g.fillRect(i * u, j * u, u, u)
                        }
                    for (let k = 0; k < 5000; k++) {
                        g.fillStyle = 'rgba(255,255,255,' + Math.random() * .03 + ')';
                        g.fillRect(Math.random() * w, Math.random() * h, 2, 2)
                    }
                    g.strokeStyle = '#6f8392';
                    g.lineWidth = 3;
                    for (let i = 0; i <= 12; i += 2) {
                        g.beginPath();
                        g.moveTo(i * u, 0);
                        g.lineTo(i * u, h);
                        g.moveTo(0, i * u);
                        g.lineTo(w, i * u);
                        g.stroke()
                    }
                    g.strokeStyle = '#fbbf24';
                    g.lineWidth = 4;
                    g.strokeRect(w / 2 - 1.3 * u, h / 2 - .7 * u, 2.6 * u, 1.4 * u);
                    g.strokeStyle = '#ef4444';
                    g.setLineDash([14, 10]);
                    g.strokeRect(w / 2 - 1.5 * u, h - 2.4 * u, 3 * u, 2 * u);
                    g.setLineDash([]);
                    g.fillStyle = 'rgba(220,38,38,.35)';
                    g.fillRect(0, h - 2 * u, 2 * u, 2 * u);
                    g.strokeStyle = '#ef4444';
                    g.strokeRect(0, h - 2 * u, 2 * u, 2 * u);
                    g.fillStyle = '#991b1b';
                    g.font = 'bold 34px system-ui';
                    g.textAlign = 'center';
                    g.fillText('PARK', u, h - u + 10)
                }
                );
                const fl = new T.Mesh(new T.PlaneGeometry(12,12),new T.MeshStandardMaterial({
                    map: ft,
                    roughness: .8
                }));
                fl.rotation.x = -Math.PI / 2;
                fl.receiveShadow = true;
                scene.add(fl);
                const glass = new T.MeshStandardMaterial({
                    color: 0xcfe3ff,
                    transparent: true,
                    opacity: .14,
                    roughness: .1,
                    depthWrite: false
                });
                [[0, -F - .15, 12.6, .3], [0, F + .15, 12.6, .3], [-F - .15, 0, .3, 12], [F + .15, 0, .3, 12]].forEach( ([x,z,w,l]) => box(w, 1, l, std(0x7b7f85, .1, .7), x, .5, z));
                {
                    const tt = tex(1024, 64, (g, w, h) => {
                        g.fillStyle = '#7b7f85';
                        g.fillRect(0, 0, w, h);
                        g.fillStyle = '#2f3237';
                        g.font = 'bold 40px system-ui';
                        g.textAlign = 'center';
                        g.fillText('F I R S T   T E C H   C H A L L E N G E', w / 2, h / 2 + 14)
                    }
                    );
                    const m = new T.Mesh(new T.PlaneGeometry(10,.6),new T.MeshStandardMaterial({
                        map: tt,
                        roughness: .8
                    }));
                    m.position.set(0, .5, F + .31);
                    scene.add(m)
                }
                hive();
                garden3();
                balls0();
                robotBuild();
                const tg = new T.BufferGeometry();
                tg.setAttribute('position', new T.BufferAttribute(new Float32Array(60 * 3),3));
                traj = new T.Line(tg,new T.LineBasicMaterial({
                    color: 0x34d399,
                    transparent: true,
                    opacity: .8
                }));
                traj.frustumCulled = false;
                scene.add(traj);
            }

            function hive() {
                const red = std(0xb91c1c, .3, .45)
                  , yel = std(0xfacc15, .3, .4)
                  , metal = std(0xcbd5e1, .9, .25)
                  , dark = std(0x26262b, .6, .4);
                const blk = std(0xf3f4f6, .2, .5)
                  , V = (x, y, z) => new T.Vector3(x,y,z);
                const tube = (a, b, r=.05) => {
                    const d = b.clone().sub(a)
                      , m = new T.Mesh(new T.BoxGeometry(2 * r,d.length(),2 * r),blk);
                    m.position.copy(a).addScaledVector(d, .5);
                    m.quaternion.setFromUnitVectors(V(0, 1, 0), d.normalize());
                    m.castShadow = true;
                    scene.add(m)
                }
                ;
                [-.55, .55].forEach(z => {
                    [-1, 1].forEach(s => {
                        tube(V(s * LX, .03, z), V(0, PH, z));
                        tube(V(s * LX, .03, -.55), V(s * LX, .03, .55), .04)
                    }
                    );
                    [.6, 1.2, 1.8, 2.4].forEach(y => {
                        const cx = LX * (1 - y / PH);
                        tube(V(-cx, y, z), V(cx, y, z), .035)
                    }
                    )
                }
                );
                box(.5, .3, .7, std(0x111114, .5, .5), 0, PH + .05, 0, null);
                const st = tex(512, 140, (g, w, h) => {
                    g.fillStyle = '#f5d20a';
                    g.fillRect(0, 0, w, h);
                    g.strokeStyle = '#2a2208';
                    g.lineWidth = 5;
                    g.strokeRect(6, 6, w - 12, h - 12);
                    for (let k = 0; k < 9; k++) {
                        g.beginPath();
                        g.arc(40 + k * 54, h - 20, 12, 0, 7);
                        g.stroke()
                    }
                    g.fillStyle = '#2a2208';
                    g.textAlign = 'center';
                    g.font = 'bold 70px system-ui';
                    g.fillText('BIOBUZZ', w / 2, h / 2 + 14);
                    g.font = 'bold 15px system-ui';
                    g.fillText('FIRST TECH CHALLENGE', w / 2, 28)
                }
                );
                [-1, 1].forEach(f => {
                    box(1.84, .54, .04, blk, 0, 1, f * .58, null);
                    const m = new T.Mesh(new T.PlaneGeometry(1.8,.5),new T.MeshStandardMaterial({
                        map: st,
                        roughness: .5
                    }));
                    m.position.set(0, 1, f * .603);
                    if (f < 0)
                        m.rotation.y = Math.PI;
                    scene.add(m)
                }
                );
                const ax = new T.Mesh(new T.CylinderGeometry(.14,.14,1.5,20),metal);
                ax.rotation.x = Math.PI / 2;
                ax.position.y = PH;
                ax.castShadow = true;
                scene.add(ax);
                beam = new T.Group();
                beam.position.set(0, PH, 0);
                scene.add(beam);
                box(6.4, .16, .3, std(0xc9ced4, .9, .3), 0, -.05, 0, beam);
                const gm = new T.MeshStandardMaterial({
                    color: 0xdbe7f3,
                    transparent: true,
                    opacity: .16,
                    roughness: .05,
                    depthWrite: false,
                    side: T.DoubleSide
                });
                const em = new T.LineBasicMaterial({
                    color: 0xffc2c2,
                    transparent: true,
                    opacity: .75
                })
                  , H = TOP - FL;
                const pane = (w, h, l, x, y, z, par) => {
                    const g = new T.BoxGeometry(w,h,l)
                      , m = new T.Mesh(g,gm);
                    m.position.set(x, y, z);
                    m.renderOrder = 2;
                    m.add(new T.LineSegments(new T.EdgesGeometry(g),em));
                    par.add(m);
                    return m
                }
                ;
                [-1, 1].forEach( (s, i) => {
                    const b = new T.Group();
                    b.position.x = s * CX;
                    beam.add(b);
                    box(2 * HW, .09, 2 * HW, std(0xb91c1c, .3, .5), 0, FL - .045, 0, b);
                    const ce = std(0xdc2626, .35, .45)
                      , ye = ce;
                    box(2 * HW + .08, .07, .07, ce, 0, TOP, HW, b);
                    box(2 * HW + .08, .07, .07, ce, 0, TOP, -HW, b);
                    box(.07, .07, 2 * HW, ye, -s * HW, TOP, 0, b);
                    box(.07, .07, 2 * HW, ye, s * HW, FL, 0, b);
                    [-1, 1].forEach(f => {
                        box(.09, H, .09, ce, -HW, FL + H / 2, f * HW, b);
                        box(.09, H, .09, ce, HW, FL + H / 2, f * HW, b);
                        box(2 * HW, .09, .09, ce, 0, FL, f * HW, b)
                    }
                    );
                    pane(.04, H, 2 * HW, -s * HW, FL + H / 2, 0, b);
                    pane(2 * HW, H, .04, 0, FL + H / 2, HW, b);
                    pane(2 * HW, H, .04, 0, FL + H / 2, -HW, b);
                    const p = new T.Group();
                    p.position.set(s * HW, FL, 0);
                    b.add(p);
                    pane(.04, H, 2 * HW, 0, H / 2, 0, p);
                    flaps[i] = p
                }
                );
                [-1, 1].forEach(s => [-1, 1].forEach(f => {
                    box(1.7, .5, .03, std(0xe5e7eb, .5, .4), s * CX, -.3, f * .185, beam).rotation.x = .6 * f
                }
                ));
                [[1, -2.48, 1, 0], [2, -1.72, 1, 0], [3, 1.72, 1, 0], [4, 2.48, 1, 0], [1, -2.48, -1, 1], [2, -1.72, -1, 1], [3, 1.72, -1, 1], [4, 2.48, -1, 1]].forEach( ([id,x,fz,back]) => {
                    const t = tex(128, 128, (g, w, h) => {
                        g.fillStyle = '#fff';
                        g.fillRect(0, 0, w, h);
                        const n = w / 6;
                        g.fillStyle = '#000';
                        g.fillRect(n * .5, n * .5, n * 5, n * 5);
                        let s = id * 9301 + 49297;
                        for (let i = 1; i < 5; i++)
                            for (let j = 1; j < 5; j++) {
                                s = (s * 233280 + 11) % 49297;
                                g.fillStyle = s % 2 ? '#fff' : '#000';
                                g.fillRect(i * n + n * .5 - n * .5 + 0, j * n + 0, n, n)
                            }
                        g.fillStyle = '#fff';
                        g.fillRect(n * .5 + n * .5, n * .5 + n * .5, 0, 0)
                    }
                    );
                    const m = new T.Mesh(new T.PlaneGeometry(2 * TS,2 * TS),new T.MeshBasicMaterial({
                        map: t
                    }));
                    m.position.set(x, -.3, fz * .215);
                    if (back)
                        m.rotation.y = Math.PI;
                    m.rotation.x = .6 * fz;
                    beam.add(m);
                    tags.push({
                        id,
                        m
                    })
                }
                );
            }

            const TW = [[-5.35, -5.35], [5.35, -5.35], [-5.35, 5.35], [5.35, 5.35]];
            function garden() {
                const pil = std(0x2a2b31, .5, .5)
                  , clr = new T.MeshStandardMaterial({
                    color: 0xcfe3ff,
                    transparent: true,
                    opacity: .22,
                    roughness: .1,
                    depthWrite: false
                })
                  , rd = std(0xdc2626, .4, .4)
                  , yl = std(0xfacc15, .3, .4)
                  , gr = std(0x22c55e, 0, .7);
                TW.forEach( ([x,z]) => {
                    const sx = Math.sign(x)
                      , sz = Math.sign(z)
                      , g = new T.Group();
                    g.position.set(x, 0, z);
                    scene.add(g);
                    box(.6, 2.2, .6, pil, 0, 1.1, 0, g);
                    const tb = new T.Mesh(new T.CylinderGeometry(.24,.24,1.7,20,1,true),clr);
                    tb.position.set(-sx * .55, .95, -sz * .55);
                    g.add(tb);
                    const cap = new T.Mesh(new T.CylinderGeometry(.3,.3,.06,20),rd);
                    cap.position.set(-sx * .55, 1.83, -sz * .55);
                    g.add(cap);
                    const base = new T.Mesh(new T.CylinderGeometry(.3,.3,.08,20),pil);
                    base.position.set(-sx * .55, .04, -sz * .55);
                    g.add(base);
                    box(.6, .08, .08, rd, -sx * .3, 1.83, -sz * .3, g);
                    for (let i = 0; i < 4; i++) {
                        const s = new T.Mesh(new T.SphereGeometry(.17,14,10),yl);
                        s.position.set(-sx * .55, .25 + i * .34, -sz * .55);
                        g.add(s)
                    }
                }
                );
                const fl = (x, z) => {
                    const g = new T.Group();
                    g.position.set(x, 0, z);
                    const s = new T.Mesh(new T.CylinderGeometry(.02,.02,.4,6),gr);
                    s.position.y = .2;
                    g.add(s);
                    const h = new T.Mesh(new T.SphereGeometry(.13,12,10),yl);
                    h.position.y = .42;
                    g.add(h);
                    for (let i = 0; i < 6; i++) {
                        const p = new T.Mesh(new T.SphereGeometry(.07,8,6),std(0xfff3a0, 0, .5))
                          , a = i * Math.PI / 3;
                        p.position.set(Math.cos(a) * .17, .42, Math.sin(a) * .17);
                        g.add(p)
                    }
                    scene.add(g)
                }
                ;
                for (let i = 0; i < 8; i++)
                    fl(-1.8 + i * .52, -5.5);
                for (let i = 0; i < 4; i++) {
                    fl(-4.6 + i * .5, 5.5);
                    fl(3.2 + i * .5, 5.5)
                }
                for (let i = 0; i < 5; i++)
                    fl(5.5, -1.2 + i * .5);
            }
            function mkBall(t, x, z, y0) {
                const r = t == 'N' ? R : .2;
                const m = new T.Mesh(mkBall.g || (mkBall.g = new T.SphereGeometry(1,24,18)),new T.MeshStandardMaterial({
                    color: t == 'N' ? 0xef4444 : 0xfacc15,
                    roughness: .3,
                    metalness: .05
                }));
                m.scale.setScalar(r);
                m.castShadow = true;
                const yy = y0 == null ? r : y0;
                m.position.set(x, yy, z);
                scene.add(m);
                balls.push({
                    m,
                    p: m.position,
                    v: new T.Vector3(),
                    t,
                    r,
                    held: false,
                    x0: x,
                    z0: z,
                    y0: yy
                })
            }
            function balls0() {
                const pts = []
                  , mk = t => {
                    for (; ; ) {
                        const x = (rnd() - .5) * 10.4
                          , z = (rnd() - .5) * 10.4;
                        if (Math.abs(x) > 4 && Math.abs(z) > 4 || Math.abs(x) < 2.7 && Math.abs(z) < 1.7 || (z > 3.4 && Math.abs(x) < 1.8) || pts.some(p => Math.hypot(p[0] - x, p[1] - z) < .9))
                            continue;
                        pts.push([x, z]);
                        mkBall(t, x, z);
                        return
                    }
                }
                ;
                for (let i = 0; i < 6; i++)
                    mk('N')
            }

            function robotBuild() {
                robot = new T.Group();
                scene.add(robot);
                const dk = std(0x1f1f23, .7, .35)
                  , rd = std(0xdc2626, .4, .4)
                  , al = std(0x9ca3af, .9, .3);
                box(1.3, .14, 1.4, dk, 0, .34, 0, robot);
                box(.08, .32, 1.4, rd, -.66, .4, 0, robot);
                box(.08, .32, 1.4, rd, .66, .4, 0, robot);
                box(1.15, .04, 1.2, new T.MeshStandardMaterial({
                    color: 0x93c5fd,
                    transparent: true,
                    opacity: .35
                }), 0, .62, .05, robot);
                [[-.5, -.5], [.5, -.5], [-.5, .5], [.5, .5]].forEach( ([x,z]) => box(.05, .28, .05, al, x, .48, z * 1.0, robot));
                box(.35, .12, .25, std(0x111, .5, .5), .3, .7, .35, robot);
                box(.3, .1, .2, std(0x2563eb, .3, .5), -.3, .7, .4, robot);
                [[-.7, -.45], [.7, -.45], [-.7, .45], [.7, .45]].forEach( ([x,z]) => {
                    const w = new T.Mesh(new T.CylinderGeometry(.25,.25,.22,24),std(0x2e2e34, .2, .8));
                    w.rotation.z = Math.PI / 2;
                    w.position.set(x, .25, z);
                    w.castShadow = true;
                    robot.add(w);
                    const h = new T.Mesh(new T.CylinderGeometry(.1,.1,.24,12),al);
                    h.rotation.z = Math.PI / 2;
                    h.position.set(x, .25, z);
                    robot.add(h)
                }
                );
                barrel = new T.Group();
                barrel.position.set(0, .55, -.1);
                robot.add(barrel);
                const tube = new T.Mesh(new T.CylinderGeometry(.2,.22,.9,24),std(0xdc2626, .8, .25));
                tube.rotation.x = Math.PI / 2;
                tube.position.z = -.45;
                tube.castShadow = true;
                barrel.add(tube);
                const ring = new T.Mesh(new T.TorusGeometry(.2,.04,10,24),al);
                ring.position.z = -.9;
                barrel.add(ring);
                roller = new T.Mesh(new T.CylinderGeometry(.12,.12,1.05,16),std(0xfacc15, .3, .6));
                roller.rotation.z = Math.PI / 2;
                roller.position.set(0, .17, -.85);
                roller.castShadow = true;
                robot.add(roller);
                pipCam = new T.PerspectiveCamera(62,4 / 3,.1,40);
                pipCam.position.set(-.35, 1.1, -.45);
                pipCam.rotation.x = .1;
                robot.add(pipCam);
                box(.14, .1, .14, dk, -.35, 1.1, -.4, robot);
            }

            function sphereBox(b, x0, y0, z0, x1, y1, z1) {
                const R = b.r
                  , p = b.p
                  , cx = Math.max(x0, Math.min(x1, p.x))
                  , cy = Math.max(y0, Math.min(y1, p.y))
                  , cz = Math.max(z0, Math.min(z1, p.z));
                let dx = p.x - cx
                  , dy = p.y - cy
                  , dz = p.z - cz
                  , d = Math.hypot(dx, dy, dz);
                if (d >= R)
                    return;
                if (d < 1e-6) {
                    dx = 0;
                    dy = 1;
                    dz = 0;
                    d = 1
                }
                const nx = dx / d
                  , ny = dy / d
                  , nz = dz / d;
                p.set(cx + nx * R, cy + ny * R, cz + nz * R);
                const vn = b.v.x * nx + b.v.y * ny + b.v.z * nz;
                if (vn < 0) {
                    const k = -1.35 * vn;
                    b.v.x += nx * k;
                    b.v.y += ny * k;
                    b.v.z += nz * k
                }
                if (ny > .5) {
                    if (Math.abs(b.v.y) < 1.5)
                        b.v.y = Math.max(0, b.v.y);
                    b.v.x *= .985;
                    b.v.z *= .985
                }
            }

            // Cesto da colmeia: colisão calculada no referencial local da gangorra (gira junto com ela)
            function basket(b, i) {
                const R = b.r
                  , sd = i ? 1 : -1
                  , p = b.p
                  , c = Math.cos(S.th)
                  , s = Math.sin(S.th)
                  , py = p.y - PH;
                let ly = -p.x * s + py * c
                  , lx = p.x * c + py * s
                  , vx = b.v.x * c + b.v.y * s
                  , vy = -b.v.x * s + b.v.y * c
                  , vz = b.v.z
                  , dx = lx - sd * CX
                  , dz = p.z
                  , ch = 0;
                if (!(ly < TOP && ly > -.07 - R && Math.abs(dx) < HW + R && Math.abs(dz) < HW + R))
                    return;
                const open = flapA[i] > .7
                  , inn = Math.abs(dx) <= HW && Math.abs(dz) <= HW;
                if (inn) {
                    if (ly < FL - .05) {
                        ly = -.07 - R;
                        if (vy > 0)
                            vy *= -.3;
                        ch = 1
                    } else {
                        const o = dx * sd
                          , ob = open ? 9 : HW - R;
                        if (o > ob) {
                            dx = sd * ob;
                            if (vx * sd > 0)
                                vx *= -.4;
                            ch = 1
                        }
                        if (-o > HW - R) {
                            dx = -sd * (HW - R);
                            if (-vx * sd > 0)
                                vx *= -.4;
                            ch = 1
                        }
                        if (Math.abs(dz) > HW - R) {
                            dz = Math.sign(dz) * (HW - R);
                            if (vz * dz > 0)
                                vz *= -.4;
                            ch = 1
                        }
                        if (ly < FL + R) {
                            ly = FL + R;
                            if (vy < 0)
                                vy *= -.35;
                            vx *= .99;
                            vz *= .99;
                            ch = 1
                        }
                        S.cnt[i][b.t == 'N' ? 'n' : 'p']++
                    }
                } else if (ly >= FL - .05 && !(open && dx * sd > HW)) {
                    if (Math.abs(dx) - HW > Math.abs(dz) - HW) {
                        dx = Math.sign(dx) * (HW + R);
                        if (vx * dx < 0)
                            vx *= -.4
                    } else {
                        dz = Math.sign(dz) * (HW + R);
                        if (vz * dz < 0)
                            vz *= -.4
                    }
                    ch = 1
                }
                if (ch) {
                    lx = sd * CX + dx;
                    p.set(lx * c - ly * s, PH + lx * s + ly * c, dz);
                    b.v.set(vx * c - vy * s, vx * s + vy * c, vz)
                }
            }

            function legs(b) {
                const R = b.r
                  , p = b.p;
                if (p.y > PH + .2 || Math.abs(p.x) > LX + .3 || Math.abs(p.z) > .9)
                    return;
                for (const z of [-.55, .55])
                    for (const s of [-1, 1]) {
                        const ax = s * LX
                          , ex = -ax
                          , ey = PH
                          , t = Math.max(0, Math.min(1, ((p.x - ax) * ex + p.y * ey) / (ex * ex + ey * ey)));
                        const dx = p.x - (ax + ex * t)
                          , dy = p.y - ey * t
                          , dz = p.z - z
                          , d = Math.hypot(dx, dy, dz)
                          , rr = R + .05;
                        if (d < rr && d > 1e-6) {
                            const nx = dx / d
                              , ny = dy / d
                              , nz = dz / d;
                            p.x += nx * (rr - d);
                            p.y += ny * (rr - d);
                            p.z += nz * (rr - d);
                            const vn = b.v.x * nx + b.v.y * ny + b.v.z * nz;
                            if (vn < 0) {
                                const k = -1.35 * vn;
                                b.v.x += nx * k;
                                b.v.y += ny * k;
                                b.v.z += nz * k
                            }
                        }
                    }
            }
            function stepBalls(dt) {
                S.cnt = [{
                    n: 0,
                    p: 0,
                    w: 0
                }, {
                    n: 0,
                    p: 0,
                    w: 0
                }];
                const L = F - R
                  , fx = -Math.sin(S.h)
                  , fz = -Math.cos(S.h)
                  , intake = keys.KeyE || S.btn;
                for (const b of balls) {
                    if (b.held)
                        continue;
                    const p = b.p
                      , R = b.r
                      , L = F - R;
                    b.v.y -= G * dt;
                    p.addScaledVector(b.v, dt);
                    if (p.y < R) {
                        p.y = R;
                        b.v.y = b.v.y < -1.5 ? -b.v.y * .45 : 0;
                        const f = Math.exp(-1.3 * dt);
                        b.v.x *= f;
                        b.v.z *= f;
                        if (Math.hypot(b.v.x, b.v.z) < .04)
                            b.v.x = b.v.z = 0
                    }
                    if (Math.abs(p.x) > L) {
                        p.x = Math.sign(p.x) * L;
                        b.v.x *= -.5
                    }
                    if (Math.abs(p.z) > L) {
                        p.z = Math.sign(p.z) * L;
                        b.v.z *= -.5
                    }
                    sphereBox(b, -.92, .73, .55, .92, 1.27, .62);
                    sphereBox(b, -.92, .73, -.62, .92, 1.27, -.55);
                    legs(b);
                    for (const t of FLW)
                        sphereBox(b, t[0] - .3, 0, t[1] - .3, t[0] + .3, 2.2, t[1] + .3);
                    basket(b, 0);
                    basket(b, 1);
                    if (p.y < .7) {
                        const rx = p.x - S.x
                          , rz = p.z - S.z
                          , fw = rx * fx + rz * fz
                          , lat = Math.abs(rx * fz - rz * fx);
                        if (intake && S.inv.length < 4 && fw > .55 && fw < 1.5 && lat < .7) {
                            b.held = true;
                            b.m.visible = false;
                            S.inv.push(b);
                            log('Intake coletou ' + (b.t == 'N' ? 'Néctar' : 'Pólen'));
                            slots();
                            continue
                        }
                        const d = Math.hypot(rx, rz);
                        if (d < .85 + R && d > 1e-4) {
                            p.x = S.x + rx / d * (.85 + R);
                            p.z = S.z + rz / d * (.85 + R);
                            b.v.x += rx / d * Math.abs(S.v) * .4;
                            b.v.z += rz / d * Math.abs(S.v) * .4
                        }
                    }
                }
                for (let i = 0; i < balls.length; i++) {
                    const a = balls[i];
                    if (a.held)
                        continue;
                    for (let j = i + 1; j < balls.length; j++) {
                        const c = balls[j];
                        if (c.held)
                            continue;
                        const dx = a.p.x - c.p.x
                          , dy = a.p.y - c.p.y
                          , dz = a.p.z - c.p.z
                          , d2 = dx * dx + dy * dy + dz * dz;
                        const rs = a.r + c.r;
                        if (d2 >= rs * rs || d2 < 1e-8)
                            continue;
                        const d = Math.sqrt(d2)
                          , nx = dx / d
                          , ny = dy / d
                          , nz = dz / d
                          , ov = (rs - d) / 2;
                        a.p.x += nx * ov;
                        a.p.y += ny * ov;
                        a.p.z += nz * ov;
                        c.p.x -= nx * ov;
                        c.p.y -= ny * ov;
                        c.p.z -= nz * ov;
                        const vn = (a.v.x - c.v.x) * nx + (a.v.y - c.v.y) * ny + (a.v.z - c.v.z) * nz;
                        if (vn < 0) {
                            const k = -1.4 * vn / 2;
                            a.v.x += nx * k;
                            a.v.y += ny * k;
                            a.v.z += nz * k;
                            c.v.x -= nx * k;
                            c.v.y -= ny * k;
                            c.v.z -= nz * k
                        }
                    }
                }
            }

            function hiveLogic(dt) {
                const l = S.cnt[0]
                  , r = S.cnt[1];
                l.w = l.n * WT.N + l.p * WT.P;
                r.w = r.n * WT.N + r.p * WT.P;
                const net = l.w - r.w;
                if (!S.tip) {
                    if (Math.abs(net) >= THR - .01) {
                        S.tip = {
                            d: net > 0 ? 1 : -1,
                            t: 0
                        };
                        S.score += 20;
                        S.tips++;
                        log('💥 Colmeia tombou para o lado ' + (net > 0 ? 'esquerdo' : 'direito') + ' (+20 pts)')
                    }
                } else if ((S.tip.t += dt) > 3.2)
                    S.tip = null;
                const tg = S.tip && S.tip.t < 2.8 ? S.tip.d * MAXT : 0;
                S.om += (70 * (tg - S.th) - 10 * S.om) * dt;
                S.th = Math.max(-MAXT * 1.05, Math.min(MAXT * 1.05, S.th + S.om * dt));
                beam.rotation.z = S.th;
                for (let i = 0; i < 2; i++) {
                    const want = S.tip && S.tip.t < 3 && ((S.tip.d == 1) == (i == 0)) ? 1.65 : 0;
                    flapA[i] += (want - flapA[i]) * Math.min(1, 6 * dt);
                    flaps[i].rotation.z = -(i ? 1 : -1) * flapA[i]
                }
            }

            function hiveLogic2(dt) {
                const lo = S.rest == 1 ? 0 : 1
                  , hi = 1 - lo;
                for (const c of S.cnt)
                    c.w = c.n * WT.N + c.p * WT.P;
                if (S.tip) {
                    if ((S.tip.t += dt) > 3)
                        S.tip = null
                } else if (S.cnt[hi].w - S.cnt[lo].w >= THR - .001) {
                    S.rest *= -1;
                    S.tip = {
                        t: 0
                    };
                    S.score += 20;
                    S.tips++;
                    log('Colmeia virou para o lado ' + (S.rest == 1 ? 'esquerdo' : 'direito') + ' (+20 pts)')
                }
                S.om += (70 * (S.rest * MAXT - S.th) - 10 * S.om) * dt;
                S.th = Math.max(-MAXT * 1.05, Math.min(MAXT * 1.05, S.th + S.om * dt));
                beam.rotation.z = S.th;
                const dump = S.rest == 1 ? 0 : 1;
                for (let i = 0; i < 2; i++) {
                    const want = S.tip && i == dump ? 1.65 : 0;
                    flapA[i] += (want - flapA[i]) * Math.min(1, 6 * dt);
                    flaps[i].rotation.z = -(i ? 1 : -1) * flapA[i]
                }
            }
            function moveRobot(dt) {
                const t = (keys.KeyW ? 1 : 0) - (keys.KeyS ? 1 : 0)
                  , r = (keys.KeyA ? 1 : 0) - (keys.KeyD ? 1 : 0);
                S.v += (t * 7 - S.v) * Math.min(1, 7 * dt);
                S.h += r * 2.4 * dt;
                S.x += -Math.sin(S.h) * S.v * dt;
                S.z += -Math.cos(S.h) * S.v * dt;
                const cx = Math.max(-1.3, Math.min(1.3, S.x))
                  , cz = Math.max(-.7, Math.min(.7, S.z));
                let dx = S.x - cx
                  , dz = S.z - cz
                  , d = Math.hypot(dx, dz);
                if (d < .85) {
                    if (d < 1e-6) {
                        dx = 0;
                        dz = 1;
                        d = 1
                    }
                    S.x = cx + dx / d * .85;
                    S.z = cz + dz / d * .85
                }
                for (const t of FLW) {
                    const ex = S.x - t[0]
                      , ez = S.z - t[1]
                      , e = Math.hypot(ex, ez);
                    if (e < 1.15 && e > 1e-6) {
                        S.x = t[0] + ex / e * 1.15;
                        S.z = t[1] + ez / e * 1.15
                    }
                }
                S.x = Math.max(-F + .8, Math.min(F - .8, S.x));
                S.z = Math.max(-F + .8, Math.min(F - .8, S.z));
                const pd = 35 * dt
                  , wd = 8 * dt;
                if (keys.KeyZ || keys.ArrowUp)
                    S.pitch = Math.min(75, S.pitch + pd);
                if (keys.KeyX || keys.ArrowDown)
                    S.pitch = Math.max(15, S.pitch - pd);
                if (keys.KeyR)
                    S.power = Math.min(30, S.power + wd);
                if (keys.KeyF)
                    S.power = Math.max(10, S.power - wd);
                if ((keys.KeyE || S.btn) && S.inv.length < 4 && (S.fc = (S.fc || 0) - dt) <= 0) {
                    const px = S.x - Math.sin(S.h)
                      , pz = S.z - Math.cos(S.h);
                    FLW.forEach( (t, k) => {
                        if (STK[k].length && S.inv.length < 4) {
                            const tx = t[0]
                              , tz = t[1];
                            if (Math.hypot(px - tx, pz - tz) < .7) {
                                const b = STK[k].pop();
                                b.m.visible = false;
                                S.inv.push(b);
                                S.fc = .4;
                                slots();
                                log('Intake pegou Pólen da flor')
                            }
                        }
                    }
                    )
                }
                robot.position.set(S.x, 0, S.z);
                robot.rotation.y = S.h;
                barrel.rotation.x = S.pitch * Math.PI / 180;
                if (keys.KeyE || S.btn)
                    roller.rotation.x += dt * 18;
            }
            const STK = [[], [], [], []];
            function garden2() {
                const pil = std(0x6b7280, .4, .55)
                  , clr = new T.MeshStandardMaterial({
                    color: 0xcfe3ff,
                    transparent: true,
                    opacity: .2,
                    roughness: .1,
                    depthWrite: false,
                    side: T.DoubleSide
                })
                  , rd = std(0xdc2626, .4, .4)
                  , yl = std(0xfacc15, .3, .4);
                TW.forEach( ([x,z], k) => {
                    const sx = Math.sign(x)
                      , sz = Math.sign(z)
                      , tx = x - sx * .55
                      , tz = z - sz * .55;
                    box(.6, 2.4, .6, pil, x, 1.2, z);
                    const tb = new T.Mesh(new T.CylinderGeometry(.34,.34,2.4,24,1,true),clr);
                    tb.position.set(tx, 1.2, tz);
                    tb.renderOrder = 2;
                    scene.add(tb);
                    const bs = new T.Mesh(new T.CylinderGeometry(.36,.36,.06,24),pil);
                    bs.position.set(tx, .03, tz);
                    scene.add(bs);
                    const rg = new T.Mesh(new T.TorusGeometry(.34,.04,8,24),rd);
                    rg.rotation.x = Math.PI / 2;
                    rg.position.set(tx, 2.4, tz);
                    scene.add(rg);
                    const hd = new T.Mesh(new T.SphereGeometry(.16,14,10),yl);
                    hd.position.set(tx, 2.62, tz);
                    scene.add(hd);
                    for (let i = 0; i < 6; i++) {
                        const a = i * Math.PI / 3
                          , p = new T.Mesh(new T.SphereGeometry(.12,10,8),rd);
                        p.position.set(tx + Math.cos(a) * .24, 2.62, tz + Math.sin(a) * .24);
                        scene.add(p)
                    }
                    for (let i = 0; i < 4; i++) {
                        mkBall('N', tx, tz, .3 + i * .58);
                        const b = balls[balls.length - 1];
                        b.held = true;
                        b.fk = k;
                        STK[k].push(b)
                    }
                }
                );
            }
            const FLW = [[-2.5, -5.5], [5.5, -2.5], [2.5, 5.5], [-5.5, 2.5]];
            function garden3() {
                const gm = new T.MeshStandardMaterial({
                    color: 0x22c55e,
                    transparent: true,
                    opacity: .28,
                    roughness: .15,
                    depthWrite: false,
                    side: T.DoubleSide
                })
                  , gs = std(0x16a34a, .2, .5);
                FLW.forEach( ([x,z], k) => {
                    const tb = new T.Mesh(new T.CylinderGeometry(.3,.3,2.2,24,1,true),gm);
                    tb.position.set(x, 1.1, z);
                    tb.renderOrder = 2;
                    scene.add(tb);
                    const bs = new T.Mesh(new T.CylinderGeometry(.33,.33,.12,24),gs);
                    bs.position.set(x, .06, z);
                    bs.castShadow = true;
                    scene.add(bs);
                    const cp = new T.Mesh(new T.CylinderGeometry(.33,.33,.14,24),gs);
                    cp.position.set(x, 2.27, z);
                    cp.castShadow = true;
                    scene.add(cp);
                    for (let i = 0; i < 5; i++) {
                        mkBall('P', x, z, .32 + i * .42);
                        const b = balls[balls.length - 1];
                        b.held = true;
                        b.fk = k;
                        STK[k].push(b)
                    }
                }
                );
            }
            function launchDir() {
                const ph = S.pitch * Math.PI / 180;
                return new T.Vector3(-Math.sin(S.h) * Math.cos(ph),Math.sin(ph),-Math.cos(S.h) * Math.cos(ph))
            }
            function spawnPos(d) {
                return new T.Vector3(S.x - Math.sin(S.h) * .1 + d.x * .95,.55 + d.y * .95,S.z - Math.cos(S.h) * .1 + d.z * .95)
            }
            function launch() {
                if (!S.inv.length) {
                    log('⚠️ Sem bolas no inventário');
                    return
                }
                const b = S.inv.pop()
                  , d = launchDir();
                b.held = false;
                b.m.visible = true;
                b.p.copy(spawnPos(d));
                b.v.copy(d).multiplyScalar(S.power);
                slots();
                log('🚀 Lançou ' + (b.t == 'N' ? 'Néctar' : 'Pólen') + ' a ' + Math.round(S.pitch) + '°')
            }
            function trajectory() {
                const d = launchDir()
                  , p = spawnPos(d)
                  , v = d.clone().multiplyScalar(S.power)
                  , a = traj.geometry.attributes.position;
                let n = 0;
                for (; n < 60; n++) {
                    a.setXYZ(n, p.x, p.y, p.z);
                    v.y -= G * .04;
                    p.addScaledVector(v, .04);
                    if (p.y < R)
                        break
                }
                for (let k = n; k < 60; k++)
                    a.setXYZ(k, p.x, Math.max(p.y, R), p.z);
                a.needsUpdate = true
            }

            function vision() {
                const ctx = $('ov').getContext('2d')
                  , W = 260
                  , H = 195;
                ctx.clearRect(0, 0, W, H);
                pipCam.updateMatrixWorld(true);
                const cp = new T.Vector3().setFromMatrixPosition(pipCam.matrixWorld)
                  , cd = pipCam.getWorldDirection(new T.Vector3())
                  , out = [];
                tags.forEach(t => {
                    t.m.updateMatrixWorld();
                    const c = new T.Vector3().setFromMatrixPosition(t.m.matrixWorld)
                      , n = new T.Vector3(0,0,1).transformDirection(t.m.matrixWorld);
                    const to = cp.clone().sub(c)
                      , d = to.length();
                    to.normalize();
                    const cn = c.clone().project(pipCam);
                    if (n.dot(to) < .25 || d > 14 || cn.z > 1 || Math.abs(cn.x) > 1 || Math.abs(cn.y) > 1)
                        return;
                    const pts = [[-1, 1], [1, 1], [1, -1], [-1, -1]].map( ([a,b]) => {
                        const v = new T.Vector3(a * TS,b * TS,0).applyMatrix4(t.m.matrixWorld).project(pipCam);
                        return [(v.x + 1) / 2 * W, (1 - v.y) / 2 * H]
                    }
                    );
                    ctx.strokeStyle = '#34d399';
                    ctx.lineWidth = 2;
                    ctx.beginPath();
                    pts.forEach( (q, i) => i ? ctx.lineTo(q[0], q[1]) : ctx.moveTo(q[0], q[1]));
                    ctx.closePath();
                    ctx.stroke();
                    ctx.fillStyle = '#34d399';
                    ctx.font = 'bold 12px system-ui';
                    ctx.fillText('ID ' + t.id, pts[0][0], pts[0][1] - 4);
                    const ang = Math.atan2(cd.x * (c.z - cp.z) - cd.z * (c.x - cp.x), cd.x * (c.x - cp.x) + cd.z * (c.z - cp.z)) * 180 / Math.PI;
                    out.push({
                        id: t.id,
                        d,
                        ang
                    })
                }
                );
                $('tg').innerHTML = out.length ? out.map(o => `<div class="row"><b style="color:var(--ok)">Tag #${o.id}</b><span>${o.d.toFixed(1)} ft · desvio ${o.ang.toFixed(0)}°</span></div>`).join('') : 'Nenhuma tag visível';
            }

            function slots() {
                $('slots').innerHTML = [0, 1, 2, 3].map(i => {
                    const b = S.inv[i];
                    return b ? `<div class="slot" style="border:0;background:${b.t == 'N' ? '#ef4444;color:#fff' : '#facc15;color:#18181b'};font-weight:700">${b.t == 'N' ? 'Néctar' : 'Pólen'}</div>` : '<div class="slot">vazio</div>'
                }
                ).join('')
            }
            function ui() {
                $('sc').textContent = S.score;
                $('tp').textContent = S.tips;
                $('iv').textContent = S.inv.length + '/4';
                $('hx').textContent = S.x.toFixed(1);
                $('hy').textContent = (-S.z).toFixed(1);
                $('hh').textContent = Math.round(S.h * 180 / Math.PI);
                $('hp').textContent = Math.round(S.pitch);
                $('hw').textContent = S.power.toFixed(0);
                $('pk').textContent = S.x < -4 && S.z > 4 ? 'dentro' : 'fora';
                const l = S.cnt[0]
                  , r = S.cnt[1];
                $('lt').textContent = `${l.n}N ${l.p}P · ${l.w.toFixed(2)} lb`;
                $('rt').textContent = `${r.n}N ${r.p}P · ${r.w.toFixed(2)} lb`;
                $('lb').style.width = Math.min(100, l.w / THR * 100) + '%';
                $('rb').style.width = Math.min(100, r.w / THR * 100) + '%';
                $('bI').textContent = 'Intake: ' + (S.btn ? 'on' : 'off')
            }
            function camUpdate() {
                const c = S.cam;
                let tx = 0
                  , ty = 1.2
                  , tz = 0;
                if (c.m) {
                    tx = S.x;
                    tz = S.z;
                    ty = .8
                }
                const az = c.az + (c.m ? S.h : 0);
                cam.position.set(tx + Math.sin(az) * Math.cos(c.el) * c.d, ty + Math.sin(c.el) * c.d, tz + Math.cos(az) * Math.cos(c.el) * c.d);
                cam.lookAt(tx, ty, tz)
            }
            function resize() {
                const w = view.clientWidth
                  , h = view.clientHeight;
                ren.setSize(w, h);
                cam.aspect = w / h;
                cam.updateProjectionMatrix()
            }
            function reset() {
                S.x = 0;
                S.z = 4.6;
                S.h = 0;
                S.v = 0
            }
            function resetBalls() {
                S.inv = [];
                balls.forEach(b => {
                    b.held = false;
                    b.m.visible = true;
                    b.p.set(b.x0, b.y0, b.z0);
                    b.v.set(0, 0, 0)
                }
                );
                STK.forEach(s => s.length = 0);
                balls.filter(b => b.fk != null).sort( (a, b) => a.y0 - b.y0).forEach(b => {
                    b.held = true;
                    STK[b.fk].push(b)
                }
                );
                slots()
            }

            let last = performance.now();
            function loop(t) {
                requestAnimationFrame(loop);
                const dt = Math.min(.033, (t - last) / 1000);
                last = t;
                moveRobot(dt);
                hiveLogic2(dt);
                for (let i = 0; i < 3; i++)
                    stepBalls(dt / 3);
                trajectory();
                ui();
                camUpdate();
                const w = view.clientWidth
                  , h = view.clientHeight;
                ren.setScissorTest(false);
                ren.setViewport(0, 0, w, h);
                ren.render(scene, cam);
                ren.setScissorTest(true);
                ren.setViewport(w - 274, 14, 260, 195);
                ren.setScissor(w - 274, 14, 260, 195);
                traj.visible = false;
                ren.render(scene, pipCam);
                traj.visible = true;
                ren.setScissorTest(false);
                vision()
            }

            build();
            resize();
            slots();
            log('Simulador BioBuzz pronto. Colete com E e lance com Espaço.');
            requestAnimationFrame(loop);
            addEventListener('resize', resize);
            addEventListener('keydown', e => {
                keys[e.code] = true;
                if (['Space', 'ArrowUp', 'ArrowDown'].includes(e.code))
                    e.preventDefault();
                if (e.code == 'Space' && !e.repeat)
                    launch()
            }
            );
            addEventListener('keyup', e => keys[e.code] = false);
            let drag = null;
            const cv = ren.domElement;
            cv.addEventListener('pointerdown', e => {
                drag = [e.clientX, e.clientY];
                cv.setPointerCapture(e.pointerId)
            }
            );
            cv.addEventListener('pointerup', () => drag = null);
            cv.addEventListener('pointermove', e => {
                if (!drag)
                    return;
                S.cam.az -= (e.clientX - drag[0]) * .005;
                S.cam.el = Math.max(.1, Math.min(1.45, S.cam.el + (e.clientY - drag[1]) * .005));
                drag = [e.clientX, e.clientY]
            }
            );
            cv.addEventListener('wheel', e => {
                e.preventDefault();
                S.cam.d = Math.max(5, Math.min(30, S.cam.d * (1 + e.deltaY * .001)))
            }
            , {
                passive: false
            });
            $('bI').onclick = () => S.btn = !S.btn;
            $('bL').onclick = launch;
            $('bR').onclick = reset;
            $('bB').onclick = resetBalls;
            $('bC').onclick = () => {
                S.cam.m ^= 1;
                S.cam.d = S.cam.m ? 9 : 17;
                $('bC').textContent = 'Câmera: ' + (S.cam.m ? 'seguir' : 'órbita')
            }
            ;