# Useful Tips for Asset Manipulation

## Google Filament Environments

[SceneView](https://github.com/SceneView/sceneview-android) is packaged with two loader 
functions to create environments, which are used for image based lighting, or a skybox
which is a 3D background image. These two functions are `HDRLoader.loadEnvironment` and
`KTXLoader.loadEnvironment`, which load `.hdr` and `.ktx` files, respectively. The `.hdr`
files are equirectangular images, while the `.ktx` files are a cubemap.

To generate the cubemap file, I downloaded the starmap file from
[NASA](https://svs.gsfc.nasa.gov/3895)
and used Preview to convert to the required `.png` format with no alpha channel. Then,
I built the tools in Google Filament by running

```
git clone https://github.com/google/filament.git
cd filament
./build.sh debug
```

This runs for a long time, after which point there will be a created directory 
`/filament/out/cmake-debug/tools/cmgen`. I placed my created `starmap_8k.png` file
inside that directory, then used the command

```
./cmgen -x starmap_512  --format=ktx --size=512 starmap_8k.png
```
which generated the folder `starmap_512` with image based lighting file `starmap_ibl.ktx`
and skybox file `starmap_skybox.ktx`. I placed these in the `assets/environments/` 
folder of the Android app. The KTX environment is added to the SceneView in the 
MainActivity using:

```
sceneView.environment = KTXLoader.loadEnvironment(
    context = this@MainActivity,
    lifecycle = lifecycle,
    iblKtxFileLocation = "environments/starmap_512_ibl.ktx",
    skyboxKtxFileLocation = "environments/starmap_512_skybox.ktx"
)
```

A similar loader using an HDR file is:

```
sceneView.environment = HDRLoader.loadEnvironment(
    context = this@MainActivity,
    lifecycle = lifecycle,
    hdrFileLocation = "environments/starmap_4k.hdr",
    createSkybox = true
)
```

As of writing, I find the KTX version to be slightly smaller file size.
