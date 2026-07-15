import * as THREE from 'three'
import ThreeGlobe from 'three-globe'
import TWEEN from '@tweenjs/tween.js'

export class EarthView {
  constructor(onCommunityClick) {
    this.group = new THREE.Group()
    this.onCommunityClick = onCommunityClick
    
    // Create globe
    this.globe = new ThreeGlobe()
      .globeImageUrl('//unpkg.com/three-globe/example/img/earth-blue-marble.jpg')
      .bumpImageUrl('//unpkg.com/three-globe/example/img/earth-topology.png')
      .showAtmosphere(true)
      .atmosphereColor('#3a228a')
      .atmosphereAltitude(0.15)
      
    this.group.add(this.globe)

    // Add a single community marker (Mock location for MVP)
    const markerData = [{
      lat: 22.54,
      lng: 114.05,
      size: 1,
      color: 'red',
      id: 'community-1'
    }]

    this.globe
      .pointsData(markerData)
      .pointColor('color')
      .pointAltitude('size')
      .pointRadius(0.5)

    // Custom invisible meshes for raycasting over points
    this.hitMeshes = []
    markerData.forEach(d => {
      const pos = this.globe.getCoords(d.lat, d.lng, 0.1)
      const geo = new THREE.SphereGeometry(2, 8, 8)
      const mat = new THREE.MeshBasicMaterial({ visible: false })
      const mesh = new THREE.Mesh(geo, mat)
      mesh.position.set(pos.x, pos.y, pos.z)
      mesh.userData = d
      this.group.add(mesh)
      this.hitMeshes.push(mesh)
    })
  }

  getGroup() {
    return this.group
  }

  initCamera(camera, controls) {
    // Animate camera to view the globe
    new TWEEN.Tween(camera.position)
      .to({ x: 0, y: 0, z: 300 }, 2000)
      .easing(TWEEN.Easing.Cubic.Out)
      .start()
      
    controls.target.set(0, 0, 0)
    controls.autoRotate = true
    controls.autoRotateSpeed = 0.5
  }

  update() {
    // any per-frame updates
  }

  onClick(raycaster) {
    const intersects = raycaster.intersectObjects(this.hitMeshes)
    if (intersects.length > 0) {
      const data = intersects[0].object.userData
      if (this.onCommunityClick) {
        this.onCommunityClick(data)
      }
    }
  }

  dispose() {
    // clean up
    this.hitMeshes.forEach(m => {
      m.geometry.dispose()
      m.material.dispose()
    })
  }
}
