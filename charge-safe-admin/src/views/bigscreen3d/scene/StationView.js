import * as THREE from 'three'
import TWEEN from '@tweenjs/tween.js'
import { getPortStatusStyle } from '../domain/sceneData.mjs'

export class StationView {
  constructor(stationData) {
    this.group = new THREE.Group()
    const ports = stationData?.ports || []

    const platform = new THREE.Mesh(
      new THREE.BoxGeometry(60, 2, 40),
      new THREE.MeshStandardMaterial({ color: 0x1e293b })
    )
    platform.position.y = -1
    this.group.add(platform)

    const columns = Math.max(1, Math.ceil(Math.sqrt(ports.length)))
    const rows = Math.ceil(ports.length / columns)
    ports.forEach((port, index) => {
      const row = Math.floor(index / columns)
      const column = index % columns
      const x = (column - (columns - 1) / 2) * 6
      const z = (row - (rows - 1) / 2) * 6
      const style = getPortStatusStyle(port.status)

      const body = new THREE.Mesh(
        new THREE.BoxGeometry(2, 8, 2),
        new THREE.MeshStandardMaterial({ color: 0x334155 })
      )
      body.position.set(x, 4, z)
      body.userData = { id: port.id, deviceId: port.deviceId, status: port.status }
      this.group.add(body)

      const indicator = new THREE.Mesh(
        new THREE.SphereGeometry(0.6, 16, 16),
        new THREE.MeshBasicMaterial({ color: style.color })
      )
      indicator.position.set(x, 8.5, z)
      indicator.userData = body.userData
      this.group.add(indicator)
    })
  }

  getGroup() {
    return this.group
  }

  initCamera(camera, controls) {
    new TWEEN.Tween(camera.position)
      .to({ x: 0, y: 30, z: 60 }, 600)
      .easing(TWEEN.Easing.Cubic.Out)
      .start()
    controls.target.set(0, 0, 0)
    controls.autoRotate = true
    controls.autoRotateSpeed = 0.5
  }

  dispose() {}
}
