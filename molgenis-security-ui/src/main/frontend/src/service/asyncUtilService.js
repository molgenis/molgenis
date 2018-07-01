// @flow
export default {
  callAfter: (toCall: Function, timeInMills: number) => {
    setTimeout(toCall, timeInMills)
  }
}
