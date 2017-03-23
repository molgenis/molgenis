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
    }, 30)
  }
}

window.addEventListener('resize', resizeThrottler, false)

export const chartColors = [
  '#3366CC',
  '#DC3912',
  '#FF9900',
  '#109618',
  '#990099',
  '#3B3EAC',
  '#0099C6',
  '#DD4477',
  '#66AA00',
  '#B82E2E',
  '#316395',
  '#994499',
  '#22AA99',
  '#AAAA11',
  '#6633CC',
  '#E67300',
  '#8B0707',
  '#329262',
  '#5574A6',
  '#3B3EAC']

export const chartColorsGradient = [
  '#ADC9FF',
  '#97B9FC',
  '#81AAFA',
  '#6C9AF7',
  '#568BF5',
  '#407CF2',
  '#2B6CF0',
  '#155DED',
  '#004EEB'
]
