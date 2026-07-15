import * as THREE from 'three'
import TWEEN from '@tweenjs/tween.js'

export class CommunityView {
  constructor(data, onStationClick) {
    this.group = new THREE.Group()
    this.onStationClick = onStationClick
    this.data = data || { areas: [] }
    
    // Create a simple ground plane
    const planeGeo = new THREE.PlaneGeometry(200, 200)
    const planeMat = new THREE.MeshStandardMaterial({ 
      color: 0x112233, 
      roughness: 0.8,
      metalness: 0.2
    })
    const plane = new THREE.Mesh(planeGeo, planeMat)
    plane.rotation.x = -Math.PI / 2
    this.group.add(plane)

    // Add some random decor buildings
    this.addDecorBuildings()

    this.hitMeshes = []
    
    // Add parking areas as interactive boxes
    const areas = this.data.areas || []
    areas.forEach((area, index) => {
      // Arrange in a circle
      const angle = (index / areas.length) * Math.PI * 2
      const radius = 50
      const x = Math.cos(angle) * radius
      const z = Math.sin(angle) * radius

      const boxGeo = new THREE.BoxGeometry(10, 5, 10)
      const boxMat = new THREE.MeshStandardMaterial({ color: 0x3b82f6 })
      const box = new THREE.Mesh(boxGeo, boxMat)
      box.position.set(x, 2.5, z)
      
      // Store data for raycasting
      box.userData = area
      
      this.group.add(box)
      this.hitMeshes.push(box)

      // Add a simple label (using sprite or HTML overlay later)
    })
  }

  addDecorBuildings() {
    const geo = new THREE.BoxGeometry(1, 1, 1)
    const mat = new THREE.MeshStandardMaterial({ color: 0x1e293b })
    
    for (let i = 0; i < 20; i++) {
      const mesh = new THREE.Mesh(geo, mat)
      const x = (Math.random() - 0.5) * 180
      const z = (Math.random() - 0.5) * 180
      // avoid center ring
      if (Math.abs(x) < 60 && Math.abs(z) < 60) continue
      
      const h = Math.random() * 20 + 5
      mesh.scale.set(10, h, 10)
      mesh.position.set(x, h/2, z)
      this.group.add(mesh)
    }
  }

  getGroup() {
    return this.group
  }

  initCamera(camera, controls) {
    // Animate camera to bird eye view
    new TWEEN.Tween(camera.position)
      .to({ x: 0, y: 150, z: 150 }, 1500)
      .easing(TWEEN.Easing.Cubic.Out)
      .start()
      
    controls.target.set(0, 0, 0)
    controls.autoRotate = false
  }

  update() {
    // Animate logic
  }

  onClick(raycaster) {
    const intersects = raycaster.intersectObjects(this.hitMeshes)
    if (intersects.length > 0) {
      const data = intersects[0].object.userData
      if (this.onStationClick) {
        this.onStationClick(data)
      }
    }
  }

  dispose() {
    // cleanup
  }
}
