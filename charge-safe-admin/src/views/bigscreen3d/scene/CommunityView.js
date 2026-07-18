import * as THREE from 'three'
import TWEEN from '@tweenjs/tween.js'

function stationPosition(index, total) {
  const columns = Math.max(1, Math.ceil(Math.sqrt(total)))
  const row = Math.floor(index / columns)
  const column = index % columns
  return {
    x: (column - (columns - 1) / 2) * 28,
    z: (row - (Math.ceil(total / columns) - 1) / 2) * 24
  }
}

export class CommunityView {
  constructor(areas, onStationClick) {
    this.group = new THREE.Group()
    this.onStationClick = onStationClick
    this.hitMeshes = []
    const stations = Array.isArray(areas) ? areas : []

    const ground = new THREE.Mesh(
      new THREE.PlaneGeometry(180, 140),
      new THREE.MeshStandardMaterial({ color: 0x102336, roughness: 0.9, metalness: 0.05 })
    )
    ground.rotation.x = -Math.PI / 2
    this.group.add(ground)

    stations.forEach((area, index) => {
      const position = stationPosition(index, stations.length)
      const color = area.faultPorts > 0 ? 0xef4444 : area.chargingPorts > 0 ? 0xf59e0b : 0x14b8a6
      const station = new THREE.Mesh(
        new THREE.BoxGeometry(16, 5, 12),
        new THREE.MeshStandardMaterial({ color, roughness: 0.55, metalness: 0.2 })
      )
      station.position.set(position.x, 2.5, position.z)
      station.userData = area
      this.group.add(station)
      this.hitMeshes.push(station)
    })
  }

  getGroup() {
    return this.group
  }

  initCamera(camera, controls) {
    new TWEEN.Tween(camera.position)
      .to({ x: 0, y: 130, z: 130 }, 700)
      .easing(TWEEN.Easing.Cubic.Out)
      .start()
    controls.target.set(0, 0, 0)
    controls.autoRotate = false
  }

  onClick(raycaster) {
    const intersects = raycaster.intersectObjects(this.hitMeshes)
    if (intersects.length > 0) this.onStationClick?.(intersects[0].object.userData)
  }

  dispose() {
    this.hitMeshes = []
  }
}
