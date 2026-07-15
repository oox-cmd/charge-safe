import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import TWEEN from '@tweenjs/tween.js'

export class Engine {
  constructor(container) {
    this.container = container
    this.scene = new THREE.Scene()
    this.scene.background = new THREE.Color(0x020914)

    const width = container.clientWidth
    const height = container.clientHeight

    this.camera = new THREE.PerspectiveCamera(45, width / height, 1, 10000)
    this.camera.position.set(0, 0, 400)

    this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
    this.renderer.setSize(width, height)
    this.renderer.setPixelRatio(window.devicePixelRatio)
    container.appendChild(this.renderer.domElement)

    this.controls = new OrbitControls(this.camera, this.renderer.domElement)
    this.controls.enableDamping = true
    this.controls.dampingFactor = 0.05
    this.controls.minDistance = 10
    this.controls.maxDistance = 1000

    this.ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
    this.scene.add(this.ambientLight)

    this.directionalLight = new THREE.DirectionalLight(0xffffff, 0.8)
    this.directionalLight.position.set(200, 500, 300)
    this.scene.add(this.directionalLight)

    this.raycaster = new THREE.Raycaster()
    this.mouse = new THREE.Vector2()

    this.activeView = null
    this.animationId = null

    // Event listeners
    window.addEventListener('resize', this.onWindowResize.bind(this))
    this.renderer.domElement.addEventListener('click', this.onClick.bind(this))

    this.animate()
  }

  onWindowResize() {
    if (!this.container) return
    const width = this.container.clientWidth
    const height = this.container.clientHeight
    this.camera.aspect = width / height
    this.camera.updateProjectionMatrix()
    this.renderer.setSize(width, height)
  }

  onClick(event) {
    const rect = this.renderer.domElement.getBoundingClientRect()
    this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
    this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

    this.raycaster.setFromCamera(this.mouse, this.camera)
    
    if (this.activeView && this.activeView.onClick) {
      this.activeView.onClick(this.raycaster)
    }
  }

  setView(view) {
    if (this.activeView) {
      this.scene.remove(this.activeView.getGroup())
      if (this.activeView.dispose) this.activeView.dispose()
    }
    this.activeView = view
    this.scene.add(view.getGroup())
    view.initCamera(this.camera, this.controls)
  }

  animate(time) {
    this.animationId = requestAnimationFrame(this.animate.bind(this))
    TWEEN.update(time)
    this.controls.update()
    
    if (this.activeView && this.activeView.update) {
      this.activeView.update()
    }
    
    this.renderer.render(this.scene, this.camera)
  }

  dispose() {
    cancelAnimationFrame(this.animationId)
    window.removeEventListener('resize', this.onWindowResize.bind(this))
    this.renderer.domElement.removeEventListener('click', this.onClick.bind(this))
    
    if (this.activeView && this.activeView.dispose) {
      this.activeView.dispose()
    }
    
    this.renderer.dispose()
    if (this.container.contains(this.renderer.domElement)) {
      this.container.removeChild(this.renderer.domElement)
    }
  }
}
