document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    let method = params.get('method') || 'hist_rgb_256';
    let index  = parseInt(params.get('index') || '0', 10);
    let distMethod= params.get('dist_method')|| 'intersect';

    const table = document.getElementById('results');
    const links = document.querySelectorAll('.method-link');

    // メソッドのリンク
    links.forEach(a => {
        const m = a.dataset.method;
        a.classList.toggle('active', m === method);
        a.addEventListener('click', e => {
            e.preventDefault();
            method = m;
            history.replaceState(null, '', `?method=${method}&index=${index}`);
            fetchAndRender();
            links.forEach(x => x.classList.toggle('active', x.dataset.method === method));
        });
    });

    // 画像クリックで再検索
    table.addEventListener('click', e => {
        if (e.target.tagName === 'IMG') {
            e.preventDefault();
            const fileNo = parseInt(e.target.alt.replace('Image ', ''), 10);
            index = fileNo - 1;
            history.replaceState(null, '', `?method=${method}&index=${index}`);
            fetchAndRender();
        }
    });

    // dist計算方法のボタン用
    const intersectBtn = document.getElementById('intersect-btn');
    const euclidBtn   = document.getElementById('euclid-btn');
    // active付与
    if (distMethod === 'intersect') {
        intersectBtn.classList.add('active');
    } else {
        euclidBtn.classList.add('active');
    }
    // EventListener
    intersectBtn.addEventListener('click', e => {
        e.preventDefault();
        distMethod = 'intersect';
        intersectBtn.classList.add('active');
        euclidBtn.classList.remove('active');
        history.replaceState(null, '', `?method=${method}&index=${index}&dist_method=${distMethod}`);
        fetchAndRender();
    });
    euclidBtn.addEventListener('click', e => {
        e.preventDefault();
        distMethod = 'euclid';
        euclidBtn.classList.add('active');
        intersectBtn.classList.remove('active');
        history.replaceState(null, '', `?method=${method}&index=${index}&dist_method=${distMethod}`);
        fetchAndRender();
    });

    // 初回描画
    fetchAndRender();

    function fetchAndRender() {
        table.innerHTML = '';
        const endpoint = `/cgi-bin/main.py?method=${method}&index=${index}&dist_method=${distMethod}`;
        fetch(endpoint)
            .then(res => res.json())
            .then(data => {
                if (!Array.isArray(data)) {
                    const msg = data.error || '想定外のレスポンスです';
                    table.insertAdjacentHTML('beforebegin',
                        `<p style="color:red">${msg}</p>`);
                    return;
                }
                let row, cellCount = 0;
                data.forEach(item => {
                    if (cellCount % 10 === 0) row = table.insertRow();
                    const cell = row.insertCell();

                    const index = item.index;
                    // indexは0始まり、fileNoは1から開始
                    const fileNo = index + 1;
                    cell.innerHTML = `
            <a href="#"><img src="../img/${fileNo}.jpg" alt="Image ${fileNo}"></a>
            <div class="info">DB index: ${item.index}</div>
<!--            <div class="info">File: ${fileNo}.jpg</div>-->
            <div class="info">Sim: ${item.similarity.toFixed(3)}</div>
          `;
                    cellCount++;
                });
            })
            .catch(err => {
                console.error(err);
                table.insertAdjacentHTML('beforebegin',
                    `<p style="color:red">通信エラー：${err}</p>`);
            });
    }
});