import request from './request'

export function simulatePortStatus(portId, status) {
  return request.post(`/api/simulator/ports/${portId}/status`, null, { params: { status } })
}

export function simulatePortFault(portId) {
  return request.post(`/api/simulator/ports/${portId}/fault`)
}

export function simulatePortRecover(portId) {
  return request.post(`/api/simulator/ports/${portId}/recover`)
}

export function simulateChargingComplete(orderId) {
  return request.post(`/api/simulator/charging/${orderId}/complete`)
}
