var _sv = null;

window.initSceneViewAsync = (canvas, onReady) => {
    console.log('Initializing SceneView in scenemanager.js');
    SceneView.create(canvas).then(sv => {
        console.log('SceneView created successfully');
        _sv = sv;
        _sv.setAutoRotate(false);
        onReady(sv);
    }).catch(err => console.error(err));
};

// Orbit the camera by a pixel-space drag delta.
// Horizontal drag rotates azimuth; vertical drag shifts camera height.
window.orbitScene = function(deltaX, deltaY) {
    if (!_sv) return;
    _sv._angle -= deltaX * 0.004;
    _sv._orbitHeight = Math.max(-2.0, Math.min(5.0, _sv._orbitHeight + deltaY * 0.0015));
    if (_sv._velocityAngle !== undefined) _sv._velocityAngle = 0;
    if (_sv._velocityHeight !== undefined) _sv._velocityHeight = 0;
};

// Zoom by a Compose scale factor (>1 = pinch-spread = zoom in, <1 = zoom out).
window.zoomScene = function(scaleFactor) {
    if (!_sv) return;
    _sv._orbitRadius = Math.max(0.5, Math.min(15.0, _sv._orbitRadius / scaleFactor));
};

// Handle scroll-wheel and trackpad pinch zoom here in JS so we can preventDefault and
// stop the browser from intercepting the gesture as a native page zoom.
// ctrlKey is set by browsers on trackpad pinch events.
document.addEventListener('wheel', function(e) {
    e.preventDefault();
    if (!_sv) return;
    var speed = e.ctrlKey ? 0.01 : 0.003;
    var scaleFactor = 1 - e.deltaY * speed;
    scaleFactor = Math.max(0.85, Math.min(1.15, scaleFactor));
    _sv._orbitRadius = Math.max(0.5, Math.min(15.0, _sv._orbitRadius / scaleFactor));
}, { passive: false });

window.addDirectionalLight = (sv, intensity, dx, dy, dz) => {
    console.log('Adding directional light');
    sv.addLight({ type: "directional", intensity: intensity, direction: [dx, dy, dz] });
};

var _primitiveMap = new Map();

window.createBox = (sv, sx, sy, sz, r, g, b) => {
    var asset = sv.createBox([0, 0, 0], [sx, sy, sz], [r, g, b]);
    if (asset) {
        var root = asset.getRoot();
        _primitiveMap.set(root, asset);
        return root;
    }
    return null;
};

window.createCylinder = (sv, radius, height, r, g, b) => {
    var asset = sv.createCylinder([0, 0, 0], radius, height, [r, g, b]);
    if (asset) {
        var root = asset.getRoot();
        _primitiveMap.set(root, asset);
        return root;
    }
    return null;
};

window.setEntityColor = (sv, entity, r, g, b, a) => {
    if (!entity) return;
    var asset = _primitiveMap.get(entity);
    if (!asset) return;
    var rm = sv._engine.getRenderableManager();
    var entities = asset.getEntities();
    for (var i = 0; i < entities.length; i++) {
        var ent = entities[i];
        var inst = rm.getInstance(ent);
        if (inst != 0) {
            var mat = rm.getMaterialInstanceAt(inst, 0);
            if (mat) {
                if (mat.setFloat4Parameter) {
                    mat.setFloat4Parameter("baseColorFactor", [r, g, b, a]);
                } else if (mat.setColor4Parameter) {
                    mat.setColor4Parameter("baseColorFactor", 0, [r, g, b, a]); // 0 = LINEAR
                } else if (mat.setParameter) {
                    mat.setParameter("baseColorFactor", [r, g, b, a]);
                }
            }
        }
    }
};

window.setEntityMaterialProperties = (sv, entity, metallic, roughness, reflectance) => {
    if (!entity) return;
    var asset = _primitiveMap.get(entity);
    if (!asset) return;
    var rm = sv._engine.getRenderableManager();
    var entities = asset.getEntities();
    for (var i = 0; i < entities.length; i++) {
        var ent = entities[i];
        var inst = rm.getInstance(ent);
        if (inst != 0) {
            var mat = rm.getMaterialInstanceAt(inst, 0);
            if (mat) {
                if (mat.setFloatParameter) {
                    mat.setFloatParameter("metallicFactor", metallic);
                    mat.setFloatParameter("roughnessFactor", roughness);
                    mat.setFloatParameter("reflectance", reflectance);
                } else if (mat.setParameter) {
                    mat.setParameter("metallicFactor", metallic);
                    mat.setParameter("roughnessFactor", roughness);
                    mat.setParameter("reflectance", reflectance);
                }
            }
        }
    }
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

