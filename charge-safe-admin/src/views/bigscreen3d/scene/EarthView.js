import * as THREE from 'three'
import ThreeGlobe from 'three-globe'
import TWEEN from '@tweenjs/tween.js'
import { buildCommunityMarkers } from '../domain/sceneData.mjs'

export class EarthView {
  constructor(communities, onCommunityClick) {
    this.group = new THREE.Group()
    this.onCommunityClick = onCommunityClick
    this.markerData = buildCommunityMarkers(communities)
    this.hitMeshes = []

    this.globe = new ThreeGlobe()
      .showAtmosphere(true)
      .atmosphereColor('#14b8a6')
      .atmosphereAltitude(0.12)
      .pointsData(this.markerData)
      .pointColor('color')
      .pointAltitude(() => 0.02)
      .pointRadius(0.5)
      .labelsData(this.markerData)
      .labelLat((marker) => marker.lat)
      .labelLng((marker) => marker.lng)
      .labelText((marker) => marker.name)
      .labelColor(() => '#e2e8f0')
      .labelAltitude(() => 0.06)
      .labelSize(() => 0.8)

    const material = this.globe.globeMaterial()
    material.color.set('#0f2744')
    material.emissive.set('#06101d')
    material.shininess = 8
    this.group.add(this.globe)

    this.markerData.forEach((marker) => {
      const position = this.globe.getCoords(marker.lat, marker.lng, 0.1)
      const geometry = new THREE.SphereGeometry(2.5, 12, 12)
      const material = new THREE.MeshBasicMaterial({ transparent: true, opacity: 0 })
      const mesh = new THREE.Mesh(geometry, material)
      mesh.position.set(position.x, position.y, position.z)
      mesh.userData = marker
      this.group.add(mesh)
      this.hitMeshes.push(mesh)
    })
  }

  getGroup() {
    return this.group
  }

  initCamera(camera, controls) {
    new TWEEN.Tween(camera.position)
      .to({ x: 0, y: 0, z: 300 }, 800)
      .easing(TWEEN.Easing.Cubic.Out)
      .start()
    controls.target.set(0, 0, 0)
    controls.autoRotate = true
    controls.autoRotateSpeed = 0.35
  }

  onClick(raycaster) {
    const intersects = raycaster.intersectObjects(this.hitMeshes)
    if (intersects.length > 0) this.onCommunityClick?.(intersects[0].object.userData)
  }

  dispose() {
    this.hitMeshes = []
  }
}
