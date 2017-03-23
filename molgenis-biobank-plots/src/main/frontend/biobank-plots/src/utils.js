import Vue from 'vue'

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
    }, 200)
  }
}

window.addEventListener('resize', resizeThrottler, false)

export const chartColors = [
  '#3366CC',
  '#DC3912',
  '#cccccc']

export const chartColorsGradient = [
  '#ADC9FF',
  '#97B9FC',
  '#81AAFA',
  '#6C9AF7',
  '#568BF5',
  '#407CF2',
  '#2B6CF0',
  '#155DED',
  '#cccccc'
]
