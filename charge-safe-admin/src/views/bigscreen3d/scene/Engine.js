import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import TWEEN from '@tweenjs/tween.js'

export class Engine {
  constructor(container) {
    if (!container) throw new Error('找不到三维渲染容器')

    this.container = container
    this.scene = new THREE.Scene()
    this.scene.background = new THREE.Color(0x020914)
    this.camera = new THREE.PerspectiveCamera(45, 1, 1, 10000)
    this.camera.position.set(0, 0, 400)
    this.disposed = false
    this.paused = false
    this.animationId = null
    this.activeView = null
    this.raycaster = new THREE.Raycaster()
    this.mouse = new THREE.Vector2()

    try {
      this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
    } catch (error) {
      throw new Error('当前浏览器或设备不支持 WebGL，无法展示三维大屏。')
    }

    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
    this.container.appendChild(this.renderer.domElement)
    this.controls = new OrbitControls(this.camera, this.renderer.domElement)
    this.controls.enableDamping = true
    this.controls.dampingFactor = 0.05
    this.controls.minDistance = 10
    this.controls.maxDistance = 1000

    this.scene.add(new THREE.AmbientLight(0xffffff, 0.6))
    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8)
    directionalLight.position.set(200, 500, 300)
    this.scene.add(directionalLight)

    this.onWindowResize = this.onWindowResize.bind(this)
    this.onClick = this.onClick.bind(this)
    this.onVisibilityChange = this.onVisibilityChange.bind(this)
    window.addEventListener('resize', this.onWindowResize)
    document.addEventListener('visibilitychange', this.onVisibilityChange)
    this.renderer.domElement.addEventListener('click', this.onClick)
    this.onWindowResize()

    if (!document.hidden) this.animate()
  }

  onWindowResize() {
    if (this.disposed || !this.container) return
    const width = Math.max(this.container.clientWidth, 1)
    const height = Math.max(this.container.clientHeight, 1)
    this.camera.aspect = width / height
    this.camera.updateProjectionMatrix()
    this.renderer.setSize(width, height, false)
  }

  onVisibilityChange() {
    if (document.hidden) this.pause()
    else this.resume()
  }

  onClick(event) {
    if (!this.activeView || this.disposed) return
    const rect = this.renderer.domElement.getBoundingClientRect()
    this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
    this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
    this.raycaster.setFromCamera(this.mouse, this.camera)
    this.activeView.onClick?.(this.raycaster)
  }

  setView(view) {
    this.disposeActiveView()
    this.activeView = view
    this.scene.add(view.getGroup())
    view.initCamera(this.camera, this.controls)
  }

  animate(time) {
    if (this.disposed || this.paused) return
    this.animationId = requestAnimationFrame(this.animate.bind(this))
    TWEEN.update(time)
    this.controls.update()
    this.activeView?.update?.()
    this.renderer.render(this.scene, this.camera)
  }

  pause() {
    this.paused = true
    if (this.animationId) cancelAnimationFrame(this.animationId)
    this.animationId = null
  }

  resume() {
    if (this.disposed || !this.paused) return
    this.paused = false
    this.animate()
  }

  disposeActiveView() {
    if (!this.activeView) return
    const group = this.activeView.getGroup()
    this.scene.remove(group)
    this.disposeObjectTree(group)
    this.activeView.dispose?.()
    this.activeView = null
  }

  disposeObjectTree(root) {
    root.traverse((object) => {
      object.geometry?.dispose?.()
      const materials = Array.isArray(object.material) ? object.material : [object.material]
      materials.filter(Boolean).forEach((material) => material.dispose?.())
    })
  }

  dispose() {
    if (this.disposed) return
    this.disposed = true
    this.pause()
    window.removeEventListener('resize', this.onWindowResize)
    document.removeEventListener('visibilitychange', this.onVisibilityChange)
    this.renderer.domElement.removeEventListener('click', this.onClick)
    this.disposeActiveView()
    this.controls.dispose()
    this.renderer.dispose()
    if (this.container.contains(this.renderer.domElement)) {
      this.container.removeChild(this.renderer.domElement)
    }
  }
}
