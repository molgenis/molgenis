import Vue from 'vue'

export function zip (arrays) {
  return arrays[0].map(function (_, i) {
    return arrays.map(function (array) { return array[i] })
  })
}

/**
 * Event bus that emits resize events.
 * Listen with $on('resize', handler)
 */
export const resizeEventBus = new Vue()

let resizeTimeout = null
const resizeThrottler = () => {
  // ignore resize events as long as an actualResizeHandler execution is in the queue
  if (!resizeTimeout) {
    resizeTimeout = setTimeout(function () {
      resizeTimeout = null
      resizeEventBus.$emit('resize')
    }, 30)
  }
}

window.addEventListener('resize', resizeThrottler, false)
