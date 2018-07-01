// @flow
export default {
  callAfter: (toCall: Function, timeInMills: number) => {
    console.log('time out')
    setTimeout(toCall, timeInMills)
  }
}
