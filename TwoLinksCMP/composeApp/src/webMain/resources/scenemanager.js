window.initSceneViewAsync = (canvas, onReady) => {
    console.log('Initializing SceneView in scenemanager.js');
    SceneView.create(canvas).then(sv => {
        console.log('SceneView created successfully');
        onReady(sv);
    }).catch(err => console.error(err));
};

window.addDirectionalLight = (sv, intensity, dx, dy, dz) => {
    console.log('Adding directional light');
    sv.addLight({ type: "directional", intensity: intensity, direction: [dx, dy, dz] });
};

window.createBox = (sv, sx, sy, sz, r, g, b) => {
    var asset = sv.createBox([0, 0, 0], [sx, sy, sz], [r, g, b]);
    return asset ? asset.getRoot() : null;
};

window.createCylinder = (sv, radius, height, r, g, b) => {
    var asset = sv.createCylinder([0, 0, 0], radius, height, [r, g, b]);
    return asset ? asset.getRoot() : null;
};

window.loadModelAsync = (sv, url, onLoaded) => {
    fetch(url).then(r => r.arrayBuffer()).then(buffer => {
        var data = new Uint8Array(buffer);
        var asset = sv._loader.createAsset(data);
        if (asset) {
            // Initialize resources and add to the shared scene
            asset.loadResources();
            sv._scene.addEntity(asset.getRoot());
            sv._scene.addEntities(asset.getRenderableEntities());

            // Return the root entity
            onLoaded(asset.getRoot());
        }
    }).catch(err => console.error("Failed to fetch model", url, err));
};

window.loadModelWithScaleAsync = (sv, url, desiredRadius, onLoaded) => {
    fetch(url).then(r => r.arrayBuffer()).then(buffer => {
        var data = new Uint8Array(buffer);
        var asset = sv._loader.createAsset(data);
        if (asset) {
            asset.loadResources();
            sv._scene.addEntity(asset.getRoot());
            sv._scene.addEntities(asset.getRenderableEntities());

            // Compute scaleToUnits: find the natural radius from bounding box half-extents
            var bb = asset.getBoundingBox();
            var halfExtent = bb ? Math.max(
                Math.abs(bb.max[0] - bb.min[0]),
                Math.abs(bb.max[1] - bb.min[1]),
                Math.abs(bb.max[2] - bb.min[2])
            ) / 2.0 : 1.0;
            var scaleFactor = halfExtent > 0 ? 0.5 * desiredRadius / halfExtent : 1.0;

            console.log('Model loaded:', url);
            console.log('  Natural size (diameter) ~', halfExtent * 2);
            console.log('  Scale factor for radius', desiredRadius, ':', scaleFactor);

            onLoaded(asset.getRoot(), scaleFactor);
        }
    }).catch(err => console.error("Failed to fetch model", url, err));
};

window.setEntityTransform = (sv, entity, m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33) => {
    if (!entity) return;
    var tcm = sv._engine.getTransformManager();
    var inst = tcm.getInstance(entity);
    if (inst != 0) {
        var mat = [m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33];
        tcm.setTransform(inst, mat);
    }
};

window.loadEnvironment = (sv, url, intensity) => {
    sv.loadEnvironment(url, intensity);
};

window.loadSkybox = (sv, url) => {
    fetch(url).then(r => r.arrayBuffer()).then(buffer => {
        try {
            var skybox = sv._engine.createSkyFromKtx1(new Uint8Array(buffer));
            sv._scene.setSkybox(skybox);
            console.log('SceneView: Skybox loaded');
        } catch (e) {
            console.warn('SceneView: loadSkybox failed', e);
        }
    }).catch(err => console.error("Failed to fetch skybox", url, err));
};