/* =========================================================
   SIMULADOR 3D – FTC BioBuzz
   Cena Three.js com robô, colmeia (gangorra), bolas (Néctar e
   Pólen), flores (torres), física simples e câmera com AprilTags.
   Unidades: pés (ft) e libras (lb).
   ========================================================= */

// Atalhos: T = namespace do Three.js | $ = getElementById
const T = THREE
              , $ = id => document.getElementById(id);

/* ---------- CONSTANTES DO CAMPO E DA COLMEIA ---------- */
const G = 32.2          // gravidade (ft/s²)
              , R = .28 // raio padrão da bola de Néctar (também usado como padrão nas colisões)
              , F = 6   // meia-largura do campo (campo de 12x12 ft)
              , PH = 3  // altura do eixo da gangorra (colmeia)
              , LX = 1.25 // distância horizontal das pernas ao centro
              , HW = .8   // meia-largura de cada cesto da colmeia
              , CX = 2.1  // distância do centro de cada cesto ao eixo central
              , FL = .12  // altura do piso interno do cesto
              , TOP = 1.1 // altura do topo do cesto
              , MAXT = .55 // inclinação máxima da gangorra (rad)
              , THR = .44  // diferença de peso (lb) necessária para virar a colmeia
              , WT = {     // peso de cada tipo de bola: N = Néctar, P = Pólen
                N: .11,
                P: .055
            }
              , TS = .33;  // meio-tamanho das AprilTags

const view = $('view'); // contêiner da cena 3D

// Objetos globais da cena
let scene, cam, ren, pipCam, robot, barrel, roller, beam, traj, seed = 7;
const flaps = []        // abas (portinholas) de despejo dos cestos
              , flapA = [0, 0] // ângulo atual de abertura de cada aba
              , balls = []     // todas as bolas da simulação
              , tags = []      // AprilTags da colmeia
              , keys = {};     // teclas atualmente pressionadas (por event.code)

/* ---------- ESTADO DO SIMULADOR ---------- */
const S = {
    x: 0,          // posição X do robô
    z: 4.6,        // posição Z do robô
    h: 0,          // direção (heading) do robô em rad
    v: 0,          // velocidade linear atual
    pitch: 50,     // ângulo do lançador (graus)
    power: 20,     // potência do lançamento (velocidade inicial)
    inv: [],       // inventário (máx. 4 bolas)
    score: 0,      // pontuação
    tips: 0,       // quantidade de tombamentos da colmeia
    btn: false,    // intake ligado pelo botão da tela
    th: .55,       // ângulo atual da gangorra (rad)
    om: 0,         // velocidade angular da gangorra
    rest: 1,       // lado em que a colmeia está apoiada (1 = esquerdo, -1 = direito)
    tip: null,     // estado do tombamento em andamento (null = nenhum)
    cnt: [{        // contagem de bolas/peso em cada cesto (0 = esq., 1 = dir.)
        n: 0,
        p: 0,
        w: 0
    }, {
        n: 0,
        p: 0,
        w: 0
    }],
    cam: {         // estado da câmera principal
        m: 0,      // modo: 0 = órbita, 1 = seguir o robô
        az: 0,     // azimute
        el: .75,   // elevação
        d: 17      // distância
    }
};

/* ---------- LANÇADOR (saída pela FRENTE do robô) ----------
   y   = altura do pivô do lançador
   z   = distância do centro do robô até o pivô, para a frente
   len = comprimento do cano (de onde a bola sai)
   Ajuste esses 3 valores para mudar o ponto de saída da bola. */
const LAUNCH = { y: .5, z: .85, len: .5 };

/* ---------- ESTADO DA PARTIDA / MENU ----------
   state: 'menu' | 'play' | 'pause' | 'end'
   timed: true = partida de 2:30 | false = treino livre */
const MATCH_TIME = 150; // 2:30 em segundos
const M = { state: 'menu', timed: true, time: MATCH_TIME };

// Gerador pseudoaleatório determinístico (LCG) – mesma semente = mesmo campo sempre
const rnd = () => (seed = (seed * 16807) % 2147483647) / 2147483647;

// Escreve uma mensagem no painel de eventos (mantém só as últimas 40 linhas)
const log = m => {
    const l = $('log')
                  , d = document.createElement('div');
    d.textContent = m;
    l.appendChild(d);
    while (l.children.length > 40)
        l.firstChild.remove();
    l.scrollTop = 1e9 // rola até o fim
}
;

// Cria um material padrão (cor, metalicidade, rugosidade)
const std = (c, m=.2, r=.5) => new T.MeshStandardMaterial({
    color: c,
    metalness: m,
    roughness: r
});

// Cria uma caixa (w=largura, h=altura, l=comprimento) na posição x,y,z.
// 'par' = objeto pai (padrão: a cena). 'sh' = projeta sombra.
function box(w, h, l, mat, x, y, z, par, sh=true) {
    const m = new T.Mesh(new T.BoxGeometry(w,h,l),mat);
    m.position.set(x, y, z);
    m.castShadow = sh;
    m.receiveShadow = true;
    (par || scene).add(m);
    return m
}

// Gera uma textura a partir de um canvas 2D desenhado pela função 'fn'
function tex(w, h, fn) {
    const c = document.createElement('canvas');
    c.width = w;
    c.height = h;
    fn(c.getContext('2d'), w, h);
    const t = new T.CanvasTexture(c);
    t.encoding = T.sRGBEncoding;
    t.anisotropy = 8; // nitidez em ângulos rasantes
    return t
}

/* =========================================================
   CONSTRUÇÃO DA CENA
   ========================================================= */
function build() {
    // Cena, fundo e neblina
    scene = new T.Scene();
    scene.background = new T.Color(0x44464a);
    scene.fog = new T.Fog(0x44464a,28,70);

    // Renderizador WebGL com sombras suaves e tone mapping
    ren = new T.WebGLRenderer({
        antialias: true
    });
    ren.setPixelRatio(Math.min(devicePixelRatio, 2));
    ren.outputEncoding = T.sRGBEncoding;
    ren.toneMapping = T.ACESFilmicToneMapping;
    ren.toneMappingExposure = .9;
    ren.shadowMap.enabled = true;
    ren.shadowMap.type = T.PCFSoftShadowMap;
    view.prepend(ren.domElement); // canvas vai para dentro de #view

    // Câmera principal
    cam = new T.PerspectiveCamera(48,1,.1,120);

    // Iluminação: hemisférica (ambiente), sol com sombras e luz de contorno azulada
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

    // ----- Piso -----
    // Chão externo escuro (fora do campo)
    const out = new T.Mesh(new T.PlaneGeometry(120,120),std(0x333333, 0, 1));
    out.rotation.x = -Math.PI / 2;
    out.position.y = -.02;
    out.receiveShadow = true;
    scene.add(out);

    // Textura do piso do campo: tabuleiro 12x12, ruído, linhas, área da colmeia e zona de parking
    const ft = tex(1024, 1024, (g, w, h) => {
        const u = w / 12; // 1 ft em pixels
        // Quadrados alternados (xadrez cinza)
        for (let i = 0; i < 12; i++)
            for (let j = 0; j < 12; j++) {
                g.fillStyle = (i + j) % 2 ? '#7d858c' : '#686f75';
                g.fillRect(i * u, j * u, u, u)
            }
        // Ruído (pontinhos claros) para dar aspecto de material
        for (let k = 0; k < 5000; k++) {
            g.fillStyle = 'rgba(255,255,255,' + Math.random() * .03 + ')';
            g.fillRect(Math.random() * w, Math.random() * h, 2, 2)
        }
        // Linhas de grade a cada 2 ft
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
        // Retângulo amarelo no centro (área da colmeia)
        g.strokeStyle = '#fbbf24';
        g.lineWidth = 4;
        g.strokeRect(w / 2 - 1.3 * u, h / 2 - .7 * u, 2.6 * u, 1.4 * u);
        // Retângulo vermelho tracejado (zona de largada)
        g.strokeStyle = '#ef4444';
        g.setLineDash([14, 10]);
        g.strokeRect(w / 2 - 1.5 * u, h - 2.4 * u, 3 * u, 2 * u);
        g.setLineDash([]);
        // Zona de parking (canto inferior esquerdo), preenchida em vermelho translúcido
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
    // Plano do campo 12x12 com a textura acima
    const fl = new T.Mesh(new T.PlaneGeometry(12,12),new T.MeshStandardMaterial({
        map: ft,
        roughness: .8
    }));
    fl.rotation.x = -Math.PI / 2;
    fl.receiveShadow = true;
    scene.add(fl);

    // Material de vidro (declarado mas não usado nesta função)
    const glass = new T.MeshStandardMaterial({
        color: 0xcfe3ff,
        transparent: true,
        opacity: .14,
        roughness: .1,
        depthWrite: false
    });

    // Paredes do campo (4 lados): [x, z, largura, comprimento]
    [[0, -F - .15, 12.6, .3], [0, F + .15, 12.6, .3], [-F - .15, 0, .3, 12], [F + .15, 0, .3, 12]].forEach( ([x,z,w,l]) => box(w, 1, l, std(0x7b7f85, .1, .7), x, .5, z));

    // Letreiro "FIRST TECH CHALLENGE" na parede de trás
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

    // Monta os elementos do jogo
    hive();       // colmeia (gangorra)
    garden3();    // flores/torres de Pólen (versão em uso)
    balls0();     // bolas de Néctar espalhadas
    robotBuild(); // robô

    // Linha vermelha que mostra a trajetória prevista do lançamento (60 pontos)
    const tg = new T.BufferGeometry();
    tg.setAttribute('position', new T.BufferAttribute(new Float32Array(60 * 3),3));
    traj = new T.Line(tg,new T.LineBasicMaterial({
        color: 0xff0000,
        transparent: true,
        opacity: .8
    }));
    traj.frustumCulled = false; // evita a linha sumir por culling
    scene.add(traj);
}

/* =========================================================
   COLMEIA (gangorra com dois cestos e AprilTags)
   ========================================================= */
function hive() {
    // Materiais
    const red = std(0xb91c1c, .3, .45)
                  , yel = std(0xfacc15, .3, .4)
                  , metal = std(0xcbd5e1, .9, .25)
                  , dark = std(0x26262b, .6, .4);
    const blk = std(0xf3f4f6, .2, .5)
                  , V = (x, y, z) => new T.Vector3(x,y,z);

    // Cria um tubo (barra quadrada) entre dois pontos a e b
    const tube = (a, b, r=.05) => {
        const d = b.clone().sub(a)
                      , m = new T.Mesh(new T.BoxGeometry(2 * r,d.length(),2 * r),blk);
        m.position.copy(a).addScaledVector(d, .5);            // posiciona no ponto médio
        m.quaternion.setFromUnitVectors(V(0, 1, 0), d.normalize()); // orienta ao longo de a→b
        m.castShadow = true;
        scene.add(m)
    }
    ;

    // Estrutura de suporte: 2 "A-frames" (z = ±.55) com pernas, travessas e degraus
    [-.55, .55].forEach(z => {
        [-1, 1].forEach(s => {
            tube(V(s * LX, .03, z), V(0, PH, z));                   // perna diagonal até o eixo
            tube(V(s * LX, .03, -.55), V(s * LX, .03, .55), .04)    // barra de base entre os dois A-frames
        }
        );
        // Travessas horizontais em 4 alturas
        [.6, 1.2, 1.8, 2.4].forEach(y => {
            const cx = LX * (1 - y / PH);
            tube(V(-cx, y, z), V(cx, y, z), .035)
        }
        )
    }
    );
    // Caixa preta no topo (mancal do eixo)
    box(.5, .3, .7, std(0x111114, .5, .5), 0, PH + .05, 0, null);

    // Textura do adesivo "BIOBUZZ" (amarelo, com favos desenhados)
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
    // Painéis brancos com o adesivo, nas duas faces (frente e trás)
    [-1, 1].forEach(f => {
        box(1.84, .54, .04, blk, 0, 1, f * .58, null);
        const m = new T.Mesh(new T.PlaneGeometry(1.8,.5),new T.MeshStandardMaterial({
            map: st,
            roughness: .5
        }));
        m.position.set(0, 1, f * .603);
        if (f < 0)
            m.rotation.y = Math.PI; // vira o adesivo para ler certo do outro lado
        scene.add(m)
    }
    );
    // Eixo cilíndrico horizontal da gangorra
    const ax = new T.Mesh(new T.CylinderGeometry(.14,.14,1.5,20),metal);
    ax.rotation.x = Math.PI / 2;
    ax.position.y = PH;
    ax.castShadow = true;
    scene.add(ax);

    // 'beam' = grupo que gira em torno do eixo (viga + cestos + tags)
    beam = new T.Group();
    beam.position.set(0, PH, 0);
    scene.add(beam);
    // Viga metálica principal
    box(6.4, .16, .3, std(0xc9ced4, .9, .3), 0, -.05, 0, beam);

    // Material de vidro dos cestos e material das arestas (contorno)
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
                  , H = TOP - FL; // altura útil do cesto

    // Cria um painel de vidro com bordas e o adiciona ao pai 'par'
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

    // Constrói os dois cestos (s = -1 esquerdo, s = +1 direito)
    [-1, 1].forEach( (s, i) => {
        const b = new T.Group();
        b.position.x = s * CX;
        beam.add(b);
        // Piso do cesto
        box(2 * HW, .09, 2 * HW, std(0xb91c1c, .3, .5), 0, FL - .045, 0, b);
        const ce = std(0xdc2626, .35, .45)
                      , ye = ce;
        // Armação superior (frente/trás) e laterais
        box(2 * HW + .08, .07, .07, ce, 0, TOP, HW, b);
        box(2 * HW + .08, .07, .07, ce, 0, TOP, -HW, b);
        box(.07, .07, 2 * HW, ye, -s * HW, TOP, 0, b);
        box(.07, .07, 2 * HW, ye, s * HW, FL, 0, b);
        // Colunas verticais e travessas inferiores
        [-1, 1].forEach(f => {
            box(.09, H, .09, ce, -HW, FL + H / 2, f * HW, b);
            box(.09, H, .09, ce, HW, FL + H / 2, f * HW, b);
            box(2 * HW, .09, .09, ce, 0, FL, f * HW, b)
        }
        );
        // Paredes de vidro: lado interno, frente, trás e tampa
        pane(.04, H, 2 * HW, -s * HW, FL + H / 2, 0, b);
        pane(2 * HW, H, .04, 0, FL + H / 2, HW, b);
        pane(2 * HW, H, .04, 0, FL + H / 2, -HW, b);
        pane(2 * HW, .04, 2 * HW, 0, TOP, 0, b);
        // Aba (portinhola) do lado externo: articulada na base, abre para despejar bolas
        const p = new T.Group();
        p.position.set(s * HW, FL, 0);
        b.add(p);
        pane(.04, H, 2 * HW, 0, H / 2, 0, p);
        flaps[i] = p
    }
    );

    // Placas inclinadas brancas sob cada cesto (suportes das AprilTags)
    [-1, 1].forEach(s => [-1, 1].forEach(f => {
        box(1.7, .5, .03, std(0xe5e7eb, .5, .4), s * CX, -.3, f * .185, beam).rotation.x = .6 * f
    }
    ));

    // AprilTags: [id, x, lado z, se está no lado de trás]. 4 na frente (z+) e 4 atrás (z-)
    [[1, -2.48, 1, 0], [2, -1.72, 1, 0], [3, 1.72, 1, 0], [4, 2.48, 1, 0], [1, -2.48, -1, 1], [2, -1.72, -1, 1], [3, 1.72, -1, 1], [4, 2.48, -1, 1]].forEach( ([id,x,fz,back]) => {
        // Textura do marcador: moldura preta e uma grade 4x4 de bits pseudoaleatórios (derivados do id)
        const t = tex(128, 128, (g, w, h) => {
            g.fillStyle = '#fff';
            g.fillRect(0, 0, w, h);
            const n = w / 6; // tamanho de cada célula
            g.fillStyle = '#000';
            g.fillRect(n * .5, n * .5, n * 5, n * 5);
            let s = id * 9301 + 49297; // semente a partir do id
            for (let i = 1; i < 5; i++)
                for (let j = 1; j < 5; j++) {
                    s = (s * 233280 + 11) % 49297;
                    g.fillStyle = s % 2 ? '#fff' : '#000';
                    g.fillRect(i * n + n * .5 - n * .5 + 0, j * n + 0, n, n)
                }
            g.fillStyle = '#fff';
            g.fillRect(n * .5 + n * .5, n * .5 + n * .5, 0, 0) // (sem efeito: retângulo de tamanho 0)
        }
        );
        const m = new T.Mesh(new T.PlaneGeometry(2 * TS,2 * TS),new T.MeshBasicMaterial({
            map: t
        }));
        m.position.set(x, -.3, fz * .215);
        if (back)
            m.rotation.y = Math.PI;   // face voltada para trás
        m.rotation.x = .6 * fz;       // acompanha a inclinação da placa
        beam.add(m);
        tags.push({
            id,
            m
        })
    }
    );
}

// Posições das 4 torres dos cantos (usadas apenas na versão antiga de garden)
const TW = [[-5.35, -5.35], [5.35, -5.35], [-5.35, 5.35], [5.35, 5.35]];

// [VERSÃO ANTIGA / NÃO USADA] Primeira versão do jardim com torres nos cantos e flores decorativas.
// Hoje a cena usa garden3().
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
        box(.6, 2.2, .6, pil, 0, 1.1, 0, g);  // pilar
        // Tubo transparente + tampa + base + braço + esferas amarelas
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
    // Flor decorativa (haste, miolo amarelo, 6 pétalas)
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
    // Fileiras de flores nas bordas
    for (let i = 0; i < 8; i++)
        fl(-1.8 + i * .52, -5.5);
    for (let i = 0; i < 4; i++) {
        fl(-4.6 + i * .5, 5.5);
        fl(3.2 + i * .5, 5.5)
    }
    for (let i = 0; i < 5; i++)
        fl(5.5, -1.2 + i * .5);
}

/* =========================================================
   BOLAS
   ========================================================= */

// Cria uma bola. t = 'N' (Néctar, vermelha, grande) ou 'P' (Pólen, amarela, pequena).
// y0 opcional = altura inicial (padrão: apoiada no chão).
function mkBall(t, x, z, y0) {
    const r = t == 'N' ? R : .2;
    // Geometria de esfera unitária compartilhada entre todas as bolas (escalada pelo raio)
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
    // Registro físico da bola (p = referência da posição da malha; x0/z0/y0 = posição inicial para reset)
    balls.push({
        m,
        p: m.position,
        v: new T.Vector3(),
        t,
        r,
        held: false,   // true = está no inventário/torre (fora da física)
        x0: x,
        z0: z,
        y0: yy
    })
}

// Espalha 6 bolas de Néctar em posições aleatórias válidas pelo campo
function balls0() {
    const pts = []
                  , mk = t => {
        for (; ; ) {
            const x = (rnd() - .5) * 10.4
                          , z = (rnd() - .5) * 10.4;
            // Rejeita posições: nos cantos, sobre a colmeia, na zona de largada, ou perto de outra bola
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

/* =========================================================
   ROBÔ
   ========================================================= */
function robotBuild() {
    robot = new T.Group();
    scene.add(robot);
    const dk = std(0x1f1f23, .7, .35)
                  , rd = std(0xdc2626, .4, .4)
                  , al = std(0x9ca3af, .9, .3);
    // Chassi, laterais vermelhas e placa superior translúcida
    box(1.3, .14, 1.4, dk, 0, .34, 0, robot);
    box(.08, .32, 1.4, rd, -.66, .4, 0, robot);
    box(.08, .32, 1.4, rd, .66, .4, 0, robot);
    box(1.15, .04, 1.2, new T.MeshStandardMaterial({
        color: 0x93c5fd,
        transparent: true,
        opacity: .35
    }), 0, .62, .05, robot);
    // Quatro colunas de alumínio nos cantos
    [[-.5, -.5], [.5, -.5], [-.5, .5], [.5, .5]].forEach( ([x,z]) => box(.05, .28, .05, al, x, .48, z * 1.0, robot));
    // Eletrônicos decorativos (Control Hub e bateria)
    box(.35, .12, .25, std(0x111, .5, .5), .3, .7, .35, robot);
    box(.3, .1, .2, std(0x2563eb, .3, .5), -.3, .7, .4, robot);
    // Quatro rodas com cubo de alumínio
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
    // Lançador ('barrel'): fica na FRENTE do robô (tubo vermelho + anel); gira para alterar o ângulo (pitch)
    barrel = new T.Group();
    barrel.position.set(0, LAUNCH.y, -LAUNCH.z);
    robot.add(barrel);
    const tube = new T.Mesh(new T.CylinderGeometry(.2,.22,LAUNCH.len + .05,24),std(0xdc2626, .8, .25));
    tube.rotation.x = Math.PI / 2;
    tube.position.z = -(LAUNCH.len + .05) / 2;
    tube.castShadow = true;
    barrel.add(tube);
    const ring = new T.Mesh(new T.TorusGeometry(.2,.04,10,24),al);
    ring.position.z = -(LAUNCH.len + .05);
    barrel.add(ring);
    // Rolo amarelo do intake (gira quando o intake está ligado)
    roller = new T.Mesh(new T.CylinderGeometry(.12,.12,1.05,16),std(0xfacc15, .3, .6));
    roller.rotation.z = Math.PI / 2;
    roller.position.set(0, .17, -.85);
    roller.castShadow = true;
    robot.add(roller);
    // Câmera do robô (usada no picture-in-picture e na detecção de AprilTags)
    pipCam = new T.PerspectiveCamera(62,4 / 3,.1,40);
    pipCam.position.set(-.35, 1.1, -.45);
    pipCam.rotation.x = .1;
    robot.add(pipCam);
    box(.14, .1, .14, dk, -.35, 1.1, -.4, robot); // suporte da câmera
}

/* =========================================================
   FÍSICA / COLISÕES
   ========================================================= */

// Colisão esfera × caixa alinhada aos eixos (AABB). b = bola; (x0,y0,z0)-(x1,y1,z1) = caixa.
function sphereBox(b, x0, y0, z0, x1, y1, z1) {
    const R = b.r
                  , p = b.p
                  // Ponto da caixa mais próximo do centro da esfera
                  , cx = Math.max(x0, Math.min(x1, p.x))
                  , cy = Math.max(y0, Math.min(y1, p.y))
                  , cz = Math.max(z0, Math.min(z1, p.z));
    let dx = p.x - cx
                  , dy = p.y - cy
                  , dz = p.z - cz
                  , d = Math.hypot(dx, dy, dz);
    if (d >= R)
        return; // sem colisão
    if (d < 1e-6) { // centro dentro da caixa: empurra para cima
        dx = 0;
        dy = 1;
        dz = 0;
        d = 1
    }
    // Normal da colisão
    const nx = dx / d
                  , ny = dy / d
                  , nz = dz / d;
    // Reposiciona a esfera para fora da caixa
    p.set(cx + nx * R, cy + ny * R, cz + nz * R);
    // Reflete a velocidade (restituição 0.35: -1.35 * vn)
    const vn = b.v.x * nx + b.v.y * ny + b.v.z * nz;
    if (vn < 0) {
        const k = -1.35 * vn;
        b.v.x += nx * k;
        b.v.y += ny * k;
        b.v.z += nz * k
    }
    // Se pousou sobre a face de cima: amortece quicadas pequenas e aplica atrito
    if (ny > .5) {
        if (Math.abs(b.v.y) < 1.5)
            b.v.y = Math.max(0, b.v.y);
        b.v.x *= .985;
        b.v.z *= .985
    }
}

// Cesto da colmeia: a colisão é calculada no referencial LOCAL da gangorra (que gira junto com ela).
// b = bola, i = índice do cesto (0 = esquerdo, 1 = direito).
function basket(b, i) {
    const R = b.r
                  , sd = i ? 1 : -1 // lado do cesto
                  , p = b.p
                  , c = Math.cos(S.th)
                  , s = Math.sin(S.th)
                  , py = p.y - PH;
    // Converte posição e velocidade do mundo para o referencial local da viga (rotação por -th)
    let ly = -p.x * s + py * c
                  , lx = p.x * c + py * s
                  , vx = b.v.x * c + b.v.y * s
                  , vy = -b.v.x * s + b.v.y * c
                  , vz = b.v.z
                  , dx = lx - sd * CX  // deslocamento relativo ao centro do cesto
                  , dz = p.z
                  , ch = 0;            // flag: houve alteração?
    // Fora da região de influência do cesto? Sai cedo.
    if (!(ly < TOP && ly > -.07 - R && Math.abs(dx) < HW + R && Math.abs(dz) < HW + R))
        return;
    const open = flapA[i] > .7  // aba aberta?
                  , inn = Math.abs(dx) <= HW && Math.abs(dz) <= HW; // centro da bola dentro da projeção do cesto
    if (inn) {
        if (ly < FL - .05) {
            // Veio por baixo do piso do cesto: empurra para baixo
            ly = -.07 - R;
            if (vy > 0)
                vy *= -.3;
            ch = 1
        } else {
            // Dentro do cesto: paredes laterais (a externa some se a aba estiver aberta)
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
            // Paredes frente/trás
            if (Math.abs(dz) > HW - R) {
                dz = Math.sign(dz) * (HW - R);
                if (vz * dz > 0)
                    vz *= -.4;
                ch = 1
            }
            // Piso do cesto
            if (ly < FL + R) {
                ly = FL + R;
                if (vy < 0)
                    vy *= -.35;
                vx *= .99;
                vz *= .99;
                ch = 1
            }
            // Conta a bola como "carga" deste cesto (Néctar ou Pólen)
            S.cnt[i][b.t == 'N' ? 'n' : 'p']++
        }
    } else if (ly >= FL - .05 && !(open && dx * sd > HW)) {
        // Fora do cesto mas encostando na parede por fora: empurra para fora do eixo mais próximo
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
        // Converte posição e velocidade de volta para o referencial do mundo
        lx = sd * CX + dx;
        p.set(lx * c - ly * s, PH + lx * s + ly * c, dz);
        b.v.set(vx * c - vy * s, vx * s + vy * c, vz)
    }
}

// Colisão da bola com as 4 pernas diagonais da estrutura (segmentos de reta com espessura)
function legs(b) {
    const R = b.r
                  , p = b.p;
    // Sai cedo se estiver longe da estrutura
    if (p.y > PH + .2 || Math.abs(p.x) > LX + .3 || Math.abs(p.z) > .9)
        return;
    for (const z of [-.55, .55])
        for (const s of [-1, 1]) {
            // Segmento da perna: de (ax, 0) até (0, PH) → vetor (ex, ey)
            const ax = s * LX
                          , ex = -ax
                          , ey = PH
                          // t = projeção da bola sobre o segmento, limitada a [0,1]
                          , t = Math.max(0, Math.min(1, ((p.x - ax) * ex + p.y * ey) / (ex * ex + ey * ey)));
            const dx = p.x - (ax + ex * t)
                          , dy = p.y - ey * t
                          , dz = p.z - z
                          , d = Math.hypot(dx, dy, dz)
                          , rr = R + .05; // raio da bola + meia espessura da perna
            if (d < rr && d > 1e-6) {
                const nx = dx / d
                              , ny = dy / d
                              , nz = dz / d;
                // Empurra para fora e reflete a velocidade
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

// Um passo de simulação de todas as bolas (gravidade, paredes, obstáculos, intake, colisão entre bolas)
function stepBalls(dt) {
    // Zera as contagens dos cestos (serão recalculadas em basket())
    S.cnt = [{
        n: 0,
        p: 0,
        w: 0
    }, {
        n: 0,
        p: 0,
        w: 0
    }];
    const L = F - R // (sombreada dentro do loop; esta não é usada)
                  , fx = -Math.sin(S.h) // vetor "frente" do robô
                  , fz = -Math.cos(S.h)
                  , intake = keys.KeyE || S.btn; // intake ativo?
    for (const b of balls) {
        if (b.held)
            continue; // bolas guardadas não entram na física
        const p = b.p
                      , R = b.r
                      , L = F - R; // limite do campo para esta bola
        // Gravidade e integração de Euler
        b.v.y -= G * dt;
        p.addScaledVector(b.v, dt);
        // Chão: quica com perda de energia e aplica atrito
        if (p.y < R) {
            p.y = R;
            b.v.y = b.v.y < -1.5 ? -b.v.y * .45 : 0;
            const f = Math.exp(-1.3 * dt);
            b.v.x *= f;
            b.v.z *= f;
            if (Math.hypot(b.v.x, b.v.z) < .04)
                b.v.x = b.v.z = 0
        }
        // Paredes do campo (quica com perda de 50%)
        if (Math.abs(p.x) > L) {
            p.x = Math.sign(p.x) * L;
            b.v.x *= -.5
        }
        if (Math.abs(p.z) > L) {
            p.z = Math.sign(p.z) * L;
            b.v.z *= -.5
        }
        // Obstáculos: barras do topo da estrutura, pernas, torres de flores e cestos
        sphereBox(b, -.92, .73, .55, .92, 1.27, .62);
        sphereBox(b, -.92, .73, -.62, .92, 1.27, -.55);
        legs(b);
        for (const t of FLW)
            sphereBox(b, t[0] - .3, 0, t[1] - .3, t[0] + .3, 2.2, t[1] + .3);
        basket(b, 0);
        basket(b, 1);
        // Interações com o robô (só para bolas baixas, no nível do chão)
        if (p.y < .7) {
            const rx = p.x - S.x
                          , rz = p.z - S.z
                          , fw = rx * fx + rz * fz            // distância à frente do robô
                          , lat = Math.abs(rx * fz - rz * fx); // distância lateral
            // Intake: coleta se estiver na zona à frente do robô e houver espaço no inventário
            if (intake && S.inv.length < 4 && fw > .55 && fw < 1.5 && lat < .7) {
                b.held = true;
                b.m.visible = false;
                S.inv.push(b);
                log('Intake coletou ' + (b.t == 'N' ? 'Néctar' : 'Pólen'));
                slots();
                continue
            }
            // Colisão com o corpo do robô (círculo de raio .85): empurra a bola para fora
            const d = Math.hypot(rx, rz);
            if (d < .85 + R && d > 1e-4) {
                p.x = S.x + rx / d * (.85 + R);
                p.z = S.z + rz / d * (.85 + R);
                b.v.x += rx / d * Math.abs(S.v) * .4;
                b.v.z += rz / d * Math.abs(S.v) * .4
            }
        }
    }
    // Colisão bola × bola (todos os pares, com troca de impulso simples)
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
            const rs = a.r + c.r; // soma dos raios
            if (d2 >= rs * rs || d2 < 1e-8)
                continue; // sem sobreposição
            const d = Math.sqrt(d2)
                          , nx = dx / d
                          , ny = dy / d
                          , nz = dz / d
                          , ov = (rs - d) / 2; // metade da sobreposição para cada
            // Separa as duas bolas
            a.p.x += nx * ov;
            a.p.y += ny * ov;
            a.p.z += nz * ov;
            c.p.x -= nx * ov;
            c.p.y -= ny * ov;
            c.p.z -= nz * ov;
            // Troca de velocidade ao longo da normal
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

/* =========================================================
   LÓGICA DA COLMEIA (tombamento)
   ========================================================= */

// [VERSÃO ANTIGA / NÃO USADA] Lógica anterior de tombamento. Hoje o loop usa hiveLogic2().
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

// Lógica ATUAL da colmeia: ela fica apoiada em um lado ('rest') e só vira quando o cesto ALTO
// ('hi') superar o cesto BAIXO ('lo') em THR libras. Ao virar, ganha +20 pontos e abre a aba de despejo.
function hiveLogic2(dt) {
    const lo = S.rest == 1 ? 0 : 1  // cesto que está embaixo (apoiado)
                  , hi = 1 - lo;    // cesto que está em cima
    // Calcula o peso de cada cesto
    for (const c of S.cnt)
        c.w = c.n * WT.N + c.p * WT.P;
    if (S.tip) {
        // Durante o tombamento: espera 3 s e libera
        if ((S.tip.t += dt) > 3)
            S.tip = null
    } else if (S.cnt[hi].w - S.cnt[lo].w >= THR - .001) {
        // Peso suficiente: inverte o lado de apoio e inicia o tombamento
        S.rest *= -1;
        S.tip = {
            t: 0
        };
        S.score += 20;
        S.tips++;
        log('Colmeia virou para o lado ' + (S.rest == 1 ? 'esquerdo' : 'direito') + ' (+20 pts)')
    }
    // Mola amortecida que leva a gangorra ao ângulo de repouso (rest * MAXT)
    S.om += (70 * (S.rest * MAXT - S.th) - 10 * S.om) * dt;
    S.th = Math.max(-MAXT * 1.05, Math.min(MAXT * 1.05, S.th + S.om * dt));
    beam.rotation.z = S.th;
    // Abre a aba do cesto que está descarregando e fecha a outra
    const dump = S.rest == 1 ? 0 : 1;
    for (let i = 0; i < 2; i++) {
        const want = S.tip && i == dump ? 1.65 : 0;
        flapA[i] += (want - flapA[i]) * Math.min(1, 6 * dt);
        flaps[i].rotation.z = -(i ? 1 : -1) * flapA[i]
    }
}

/* =========================================================
   MOVIMENTO DO ROBÔ
   ========================================================= */
function moveRobot(dt) {
    // Entradas: t = frente/trás (W/S), r = girar (A = anti-horário, D = horário)
    const t = (keys.KeyW ? 1 : 0) - (keys.KeyS ? 1 : 0)
                  , r = (keys.KeyA ? 1 : 0) - (keys.KeyD ? 1 : 0);
    // Aceleração suave até a velocidade desejada (7 ft/s)
    S.v += (t * 7 - S.v) * Math.min(1, 7 * dt);
    S.h += r * 2.4 * dt; // gira a 2.4 rad/s
    // Avança na direção do heading
    S.x += -Math.sin(S.h) * S.v * dt;
    S.z += -Math.cos(S.h) * S.v * dt;

    // Colisão do robô com a colmeia: tratada como retângulo central; mantém distância mínima de .85
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
    // Colisão do robô com as torres das flores (raio 1.15)
    for (const t of FLW) {
        const ex = S.x - t[0]
                      , ez = S.z - t[1]
                      , e = Math.hypot(ex, ez);
        if (e < 1.15 && e > 1e-6) {
            S.x = t[0] + ex / e * 1.15;
            S.z = t[1] + ez / e * 1.15
        }
    }
    // Mantém o robô dentro do campo
    S.x = Math.max(-F + .8, Math.min(F - .8, S.x));
    S.z = Math.max(-F + .8, Math.min(F - .8, S.z));

    // Ajuste do lançador: pd = velocidade de mudança de ângulo | wd = de potência
    const pd = 35 * dt
                  , wd = 8 * dt;
    if (keys.KeyZ || keys.ArrowUp)
        S.pitch = Math.min(75, S.pitch + pd);   // sobe o ângulo (máx. 75°)
    if (keys.KeyX || keys.ArrowDown)
        S.pitch = Math.max(15, S.pitch - pd);   // desce o ângulo (mín. 15°)
    if (keys.KeyR)
        S.power = Math.min(30, S.power + wd);   // mais potência (máx. 30)
    if (keys.KeyF)
        S.power = Math.max(10, S.power - wd);   // menos potência (mín. 10)

    // Intake também coleta Pólen das torres de flores (a cada 0,4 s)
    if ((keys.KeyE || S.btn) && S.inv.length < 4 && (S.fc = (S.fc || 0) - dt) <= 0) {
        // Ponto 1 ft à frente do robô
        const px = S.x - Math.sin(S.h)
                      , pz = S.z - Math.cos(S.h);
        FLW.forEach( (t, k) => {
            if (STK[k].length && S.inv.length < 4) {
                const tx = t[0]
                              , tz = t[1];
                if (Math.hypot(px - tx, pz - tz) < .7) {
                    const b = STK[k].pop(); // retira a bola do topo da pilha
                    b.m.visible = false;
                    S.inv.push(b);
                    S.fc = .4;              // tempo de espera até a próxima coleta
                    slots();
                    log('Intake pegou Pólen da flor')
                }
            }
        }
        )
    }
    // Aplica posição/rotação ao modelo 3D e o ângulo do lançador
    robot.position.set(S.x, 0, S.z);
    robot.rotation.y = S.h;
    barrel.rotation.x = S.pitch * Math.PI / 180;
    // Gira o rolo do intake quando ativo
    if (keys.KeyE || S.btn)
        roller.rotation.x += dt * 18;
}

/* =========================================================
   FLORES (torres de Pólen)
   ========================================================= */

// Pilhas de bolas guardadas em cada torre (índice = torre). Pop() retira a de cima.
const STK = [[], [], [], []];

// [VERSÃO INTERMEDIÁRIA / NÃO USADA] Torres vermelhas nos cantos com Néctar. Hoje usa-se garden3().
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
        // Empilha 4 bolas de Néctar dentro do tubo
        for (let i = 0; i < 4; i++) {
            mkBall('N', tx, tz, .3 + i * .58);
            const b = balls[balls.length - 1];
            b.held = true;
            b.fk = k;          // índice da torre
            STK[k].push(b)
        }
    }
    );
}

// Posições das 4 torres de flores em uso (uma em cada lado do campo)
const FLW = [[-2.5, -5.5], [5.5, -2.5], [2.5, 5.5], [-5.5, 2.5]];

// Torres atuais: tubo de vidro verde com base e tampa, cada uma com 5 bolas de Pólen empilhadas
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
        // Tubo transparente
        const tb = new T.Mesh(new T.CylinderGeometry(.3,.3,2.2,24,1,true),gm);
        tb.position.set(x, 1.1, z);
        tb.renderOrder = 2;
        scene.add(tb);
        // Base
        const bs = new T.Mesh(new T.CylinderGeometry(.33,.33,.12,24),gs);
        bs.position.set(x, .06, z);
        bs.castShadow = true;
        scene.add(bs);
        // Tampa
        const cp = new T.Mesh(new T.CylinderGeometry(.33,.33,.14,24),gs);
        cp.position.set(x, 2.27, z);
        cp.castShadow = true;
        scene.add(cp);
        // 5 bolas de Pólen empilhadas (guardadas: 'held')
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

/* =========================================================
   LANÇADOR
   ========================================================= */

// Direção de lançamento (vetor unitário) a partir do heading e do pitch
function launchDir() {
    const ph = S.pitch * Math.PI / 180;
    return new T.Vector3(-Math.sin(S.h) * Math.cos(ph),Math.sin(ph),-Math.cos(S.h) * Math.cos(ph))
}

// Posição inicial da bola lançada: boca do cano, na FRENTE do robô
// (pivô à frente + comprimento do cano projetado na direção do lançamento)
function spawnPos(d) {
    const ph = S.pitch * Math.PI / 180
                  , out = LAUNCH.z + Math.cos(ph) * LAUNCH.len; // distância horizontal à frente do centro
    return new T.Vector3(S.x - Math.sin(S.h) * out, LAUNCH.y + Math.sin(ph) * LAUNCH.len, S.z - Math.cos(S.h) * out)
}

// Lança a última bola do inventário
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
    b.v.copy(d).multiplyScalar(S.power); // velocidade inicial = direção × potência
    slots();
    log('🚀 Lançou ' + (b.t == 'N' ? 'Néctar' : 'Pólen') + ' a ' + Math.round(S.pitch) + '°')
}

// Calcula a trajetória prevista (60 passos de 0,04 s) e atualiza a linha vermelha
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
            break // parou ao tocar o chão
    }
    // Preenche os pontos restantes no último ponto válido
    for (let k = n; k < 60; k++)
        a.setXYZ(k, p.x, Math.max(p.y, R), p.z);
    a.needsUpdate = true
}

/* =========================================================
   VISÃO COMPUTACIONAL SIMULADA (detecção de AprilTags)
   ========================================================= */
function vision() {
    const ctx = $('ov').getContext('2d')
                  , W = 260
                  , H = 195;
    ctx.clearRect(0, 0, W, H);
    pipCam.updateMatrixWorld(true);
    // cp = posição da câmera | cd = direção para onde olha | out = tags detectadas
    const cp = new T.Vector3().setFromMatrixPosition(pipCam.matrixWorld)
                  , cd = pipCam.getWorldDirection(new T.Vector3())
                  , out = [];
    tags.forEach(t => {
        t.m.updateMatrixWorld();
        // c = centro da tag | n = normal da tag (para onde sua face aponta)
        const c = new T.Vector3().setFromMatrixPosition(t.m.matrixWorld)
                      , n = new T.Vector3(0,0,1).transformDirection(t.m.matrixWorld);
        const to = cp.clone().sub(c)  // vetor da tag até a câmera
                      , d = to.length();   // distância
        to.normalize();
        const cn = c.clone().project(pipCam); // centro da tag em coordenadas de tela (-1..1)
        // Descarta se: a face não olha para a câmera, muito longe, atrás da câmera ou fora do campo de visão
        if (n.dot(to) < .25 || d > 14 || cn.z > 1 || Math.abs(cn.x) > 1 || Math.abs(cn.y) > 1)
            return;
        // Projeta os 4 cantos da tag para pixels do overlay
        const pts = [[-1, 1], [1, 1], [1, -1], [-1, -1]].map( ([a,b]) => {
            const v = new T.Vector3(a * TS,b * TS,0).applyMatrix4(t.m.matrixWorld).project(pipCam);
            return [(v.x + 1) / 2 * W, (1 - v.y) / 2 * H]
        }
        );
        // Desenha o contorno verde e o rótulo "ID n"
        ctx.strokeStyle = '#34d399';
        ctx.lineWidth = 2;
        ctx.beginPath();
        pts.forEach( (q, i) => i ? ctx.lineTo(q[0], q[1]) : ctx.moveTo(q[0], q[1]));
        ctx.closePath();
        ctx.stroke();
        ctx.fillStyle = '#34d399';
        ctx.font = 'bold 12px system-ui';
        ctx.fillText('ID ' + t.id, pts[0][0], pts[0][1] - 4);
        // Desvio angular (graus) entre a direção da câmera e a direção da tag (no plano XZ)
        const ang = Math.atan2(cd.x * (c.z - cp.z) - cd.z * (c.x - cp.x), cd.x * (c.x - cp.x) + cd.z * (c.z - cp.z)) * 180 / Math.PI;
        out.push({
            id: t.id,
            d,
            ang
        })
    }
    );
    // Atualiza a lista de tags visíveis no painel
    $('tg').innerHTML = out.length ? out.map(o => `<div class="row"><b style="color:var(--ok)">Tag #${o.id}</b><span>${o.d.toFixed(1)} ft · desvio ${o.ang.toFixed(0)}°</span></div>`).join('') : 'Nenhuma tag visível';
}

/* =========================================================
   INTERFACE (HUD e painel)
   ========================================================= */

// Redesenha os 4 slots do inventário (colorindo conforme o tipo da bola)
function slots() {
    $('slots').innerHTML = [0, 1, 2, 3].map(i => {
        const b = S.inv[i];
        return b ? `<div class="slot" style="border:0;background:${b.t == 'N' ? '#ef4444;color:#fff' : '#facc15;color:#18181b'};font-weight:700">${b.t == 'N' ? 'Néctar' : 'Pólen'}</div>` : '<div class="slot">vazio</div>'
    }
    ).join('')
}

// Atualiza todos os textos e barras de HUD/painel a cada quadro
function ui() {
    $('sc').textContent = S.score;
    $('tp').textContent = S.tips;
    $('iv').textContent = S.inv.length + '/4';
    $('hx').textContent = S.x.toFixed(1);
    $('hy').textContent = (-S.z).toFixed(1);               // Y exibido = -Z do mundo
    $('hh').textContent = Math.round(S.h * 180 / Math.PI);  // heading em graus
    $('hp').textContent = Math.round(S.pitch);
    $('hw').textContent = S.power.toFixed(0);
    // Parking: canto inferior esquerdo (x < -4 e z > 4)
    $('pk').textContent = S.x < -4 && S.z > 4 ? 'dentro' : 'fora';
    const l = S.cnt[0]
                  , r = S.cnt[1];
    $('lt').textContent = `${l.n}N ${l.p}P · ${l.w.toFixed(2)} lb`;
    $('rt').textContent = `${r.n}N ${r.p}P · ${r.w.toFixed(2)} lb`;
    // Barras: peso / limite de tombamento (máx. 100%)
    $('lb').style.width = Math.min(100, l.w / THR * 100) + '%';
    $('rb').style.width = Math.min(100, r.w / THR * 100) + '%';
    $('bI').textContent = 'Intake: ' + (S.btn ? 'on' : 'off')
}

// Posiciona a câmera principal (órbita em volta da colmeia ou seguindo o robô)
function camUpdate() {
    const c = S.cam;
    let tx = 0
                  , ty = 1.2
                  , tz = 0; // ponto para onde a câmera olha
    if (c.m) { // modo seguir: alvo = robô
        tx = S.x;
        tz = S.z;
        ty = .8
    }
    const az = c.az + (c.m ? S.h : 0); // no modo seguir, acompanha também a rotação do robô
    // Coordenadas esféricas → cartesianas
    cam.position.set(tx + Math.sin(az) * Math.cos(c.el) * c.d, ty + Math.sin(c.el) * c.d, tz + Math.cos(az) * Math.cos(c.el) * c.d);
    cam.lookAt(tx, ty, tz)
}

// Ajusta o renderizador e a câmera ao tamanho do contêiner
function resize() {
    const w = view.clientWidth
                  , h = view.clientHeight;
    ren.setSize(w, h);
    cam.aspect = w / h;
    cam.updateProjectionMatrix()
}

// Volta o robô à posição inicial
function reset() {
    S.x = 0;
    S.z = 4.6;
    S.h = 0;
    S.v = 0
}

// Devolve todas as bolas à posição inicial e refaz as pilhas das torres
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
    // Bolas que pertencem a torres (fk definido) voltam a ficar guardadas, da mais baixa à mais alta
    balls.filter(b => b.fk != null).sort( (a, b) => a.y0 - b.y0).forEach(b => {
        b.held = true;
        STK[b.fk].push(b)
    }
    );
    slots()
}

/* =========================================================
   LOOP PRINCIPAL
   ========================================================= */
let last = performance.now();
function loop(t) {
    requestAnimationFrame(loop);
    const dt = Math.min(.033, (t - last) / 1000); // delta de tempo limitado (evita saltos)
    last = t;
    if (M.state === 'play') {
        // Só atualiza o jogo durante a partida (menu, pausa e fim congelam tudo)
        moveRobot(dt);
        hiveLogic2(dt);
        // Física em 3 sub-passos por quadro (mais estável)
        for (let i = 0; i < 3; i++)
            stepBalls(dt / 3);
    } else if (M.state === 'menu') {
        S.cam.az += dt * .15; // câmera gira devagar no fundo do menu
    }
    clock(M.state === 'play' ? dt : 0); // cronômetro (dt = 0 fora da partida)
    trajectory();
    ui();
    camUpdate();
    const w = view.clientWidth
                  , h = view.clientHeight;
    // 1) Renderiza a visão principal em tela cheia
    ren.setScissorTest(false);
    ren.setViewport(0, 0, w, h);
    ren.render(scene, cam);
    // 2) Renderiza a câmera do robô no canto inferior direito (viewport + scissor)
    ren.setScissorTest(true);
    ren.setViewport(w - 274, 14, 260, 195);
    ren.setScissor(w - 274, 14, 260, 195);
    traj.visible = false;   // esconde a linha de trajetória na câmera do robô
    ren.render(scene, pipCam);
    traj.visible = true;
    ren.setScissorTest(false);
    // 3) Desenha as detecções de AprilTag por cima
    vision()
}

/* =========================================================
   INICIALIZAÇÃO
   ========================================================= */
build();
resize();
slots();
log('Simulador BioBuzz pronto. Colete com E e lance com Espaço.');
requestAnimationFrame(loop);
addEventListener('resize', resize);

/* ---------- Teclado ---------- */
addEventListener('keydown', e => {
    keys[e.code] = true;
    // Evita rolagem da página com Espaço e setas
    if (['Space', 'ArrowUp', 'ArrowDown'].includes(e.code))
        e.preventDefault();
    // Espaço lança (ignora repetição ao segurar a tecla) – só durante a partida
    if (e.code == 'Space' && !e.repeat && M.state === 'play')
        launch();
    // Esc ou P: pausa / continua
    if ((e.code == 'Escape' || e.code == 'KeyP') && !e.repeat)
        pause()
}
);
addEventListener('keyup', e => keys[e.code] = false);

/* ---------- Mouse: arrastar = órbita, roda = zoom ---------- */
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
    S.cam.az -= (e.clientX - drag[0]) * .005;                                            // horizontal: azimute
    S.cam.el = Math.max(.1, Math.min(1.45, S.cam.el + (e.clientY - drag[1]) * .005));    // vertical: elevação (limitada)
    drag = [e.clientX, e.clientY]
}
);
cv.addEventListener('wheel', e => {
    e.preventDefault();
    S.cam.d = Math.max(5, Math.min(30, S.cam.d * (1 + e.deltaY * .001))) // zoom entre 5 e 30
}
, {
    passive: false
});

/* ---------- Botões do painel ---------- */
$('bI').onclick = () => S.btn = !S.btn;  // liga/desliga intake
$('bL').onclick = launch;                // lançar
$('bR').onclick = reset;                 // reset do robô
$('bB').onclick = resetBalls;            // reset das bolas
$('bC').onclick = () => {                // alterna modo da câmera
    S.cam.m ^= 1;
    S.cam.d = S.cam.m ? 9 : 17;          // distância padrão de cada modo
    $('bC').textContent = 'Câmera: ' + (S.cam.m ? 'seguir' : 'órbita')
}
;


/* =========================================================
   MENU, PARTIDA E CRONÔMETRO
   ========================================================= */

// Mostra uma tela do menu pelo id (ou esconde o menu inteiro com null)
function showScreen(id) {
    $('menu').hidden = !id;
    document.querySelectorAll('.scr').forEach(s => s.hidden = s.id !== id)
}

// Solta todas as teclas (evita o robô "andar sozinho" ao voltar para o jogo)
function releaseKeys() {
    for (const k in keys)
        keys[k] = false;
    S.btn = false
}

// Atualiza o cronômetro. dt > 0 só durante a partida; ao zerar, encerra a partida.
function clock(dt) {
    if (dt && M.timed) {
        M.time -= dt;
        if (M.time <= 0) {
            M.time = 0;
            endMatch()
        }
    }
    const t = Math.ceil(M.time);
    $('tm').textContent = M.timed ? Math.floor(t / 60) + ':' + String(t % 60).padStart(2, '0') : '∞ treino';
    // Últimos 10 s em vermelho
    $('tm').style.color = M.timed && M.state === 'play' && M.time <= 10 ? '#f87171' : ''
}

// Começa uma nova partida (timed = true: 2:30 | false: treino livre sem tempo)
function newMatch(timed) {
    M.timed = timed;
    M.time = MATCH_TIME;
    reset();       // robô na posição inicial
    resetBalls();  // bolas e torres de Pólen de volta ao início
    Object.assign(S, {
        score: 0,
        tips: 0,
        tip: null,
        rest: 1,   // colmeia volta a apoiar no lado esquerdo
        th: .55,
        om: 0,
        pitch: 50,
        power: 20,
        fc: 0
    });
    flapA[0] = flapA[1] = 0;
    releaseKeys();
    $('log').innerHTML = '';
    M.state = 'play';
    showScreen(null);
    log(timed ? 'Partida iniciada: 2:30 de teleoperado.' : 'Treino livre iniciado (sem tempo).')
}

// Fim da partida: se o robô estiver no estacionamento, ganha +5 pontos
function endMatch() {
    if (M.state !== 'play')
        return;
    M.state = 'end';
    releaseKeys();
    const parked = S.x < -4 && S.z > 4;   // mesma regra do indicador "parking" do HUD
    if (parked)
        S.score += 5;
    log(parked ? 'Fim da partida: robô estacionado (+5 pts).' : 'Fim da partida: robô fora do estacionamento.');
    // Resumo da pontuação
    $('eTips').textContent = S.tips + ' × 20 = ' + (S.tips * 20);
    $('ePark').textContent = parked ? '+5 (no estacionamento)' : '0 (fora do estacionamento)';
    $('eTotal').textContent = S.score;
    showScreen('mEnd')
}

// Pausa / continua (Esc, P ou botão Menu)
function pause() {
    if (M.state === 'play') {
        M.state = 'pause';
        releaseKeys();
        showScreen('mPause')
    } else if (M.state === 'pause') {
        M.state = 'play';
        showScreen(null)
    }
}

// Volta ao menu principal
function toMenu() {
    M.state = 'menu';
    showScreen('mMain')
}

/* ---------- Botões do menu ---------- */
$('bStart').onclick = () => newMatch(true);
$('bFree').onclick = () => newMatch(false);
$('bHelp').onclick = () => showScreen('mHelp');
$('bBack').onclick = () => showScreen('mMain');
$('bResume').onclick = pause;
$('bRestart').onclick = () => newMatch(M.timed);
$('bFinish').onclick = endMatch;
$('bQuit').onclick = toMenu;
$('bAgain').onclick = () => newMatch(M.timed);
$('bQuit2').onclick = toMenu;
$('bM').onclick = pause;

// Tira o foco dos botões após clicar (senão a barra de espaço "clicaria" no botão em vez de lançar)
document.addEventListener('click', e => {
    if (e.target.tagName === 'BUTTON')
        e.target.blur()
});

showScreen('mMain'); // abre no menu principal