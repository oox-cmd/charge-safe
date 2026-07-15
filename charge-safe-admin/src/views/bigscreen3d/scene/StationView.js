import * as THREE from 'three'
import TWEEN from '@tweenjs/tween.js'

export class StationView {
  constructor(stationData) {
    this.group = new THREE.Group()
    this.stationData = stationData
    
    // Platform
    const planeGeo = new THREE.BoxGeometry(60, 2, 40)
    const planeMat = new THREE.MeshStandardMaterial({ color: 0x1e293b })
    const plane = new THREE.Mesh(planeGeo, planeMat)
    plane.position.y = -1
    this.group.add(plane)

    const ports = stationData.ports || []
    
    // Map status to color
    const getStatusColor = (status) => {
      switch (status) {
        case 'IDLE': return 0x10b981 // Green
        case 'CHARGING': return 0x3b82f6 // Blue
        case 'FAULT': return 0xef4444 // Red
        case 'OFFLINE': return 0x64748b // Gray
        default: return 0xeab308 // Yellow (RESERVED)
      }
    }

    // Layout ports in a grid
    const cols = Math.ceil(Math.sqrt(ports.length))
    const spacing = 6
    
    ports.forEach((port, index) => {
      const row = Math.floor(index / cols)
      const col = index % cols
      
      const x = (col - cols/2) * spacing + spacing/2
      const z = (row - Math.ceil(ports.length/cols)/2) * spacing + spacing/2
      
      // Port Body
      const bodyGeo = new THREE.BoxGeometry(2, 8, 2)
      const bodyMat = new THREE.MeshStandardMaterial({ color: 0x334155 })
      const body = new THREE.Mesh(bodyGeo, bodyMat)
      body.position.set(x, 4, z)
      this.group.add(body)
      
      // Status Indicator Light
      const lightGeo = new THREE.SphereGeometry(0.5, 16, 16)
      const lightColor = getStatusColor(port.status)
      const lightMat = new THREE.MeshBasicMaterial({ color: lightColor })
      const light = new THREE.Mesh(lightGeo, lightMat)
      light.position.set(x, 8.5, z)
      this.group.add(light)
    })
  }

  getGroup() {
    return this.group
  }

  initCamera(camera, controls) {
    new TWEEN.Tween(camera.position)
      .to({ x: 0, y: 30, z: 60 }, 1000)
      .easing(TWEEN.Easing.Cubic.Out)
      .start()
      
    controls.target.set(0, 0, 0)
    controls.autoRotate = true
    controls.autoRotateSpeed = 1.0
  }

  update() {
  }

  dispose() {
  }
}
