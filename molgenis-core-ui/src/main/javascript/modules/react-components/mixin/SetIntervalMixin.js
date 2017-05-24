/**
 * Mixin that you can use for components that need to call setInterval.
 * It'll keep track of the intervalIDs returned by the calls to the methods
 * and clear them before unmounting.
 */
var SetIntervalMixin = {
    componentWillMount: function () {
        this.intervals = [];
    },
    setInterval: function () {
        this.intervals.push(setInterval.apply(null, arguments));
    },
    clearIntervals: function () {
        this.intervals.forEach(clearInterval);
    },
    componentWillUnmount: function () {
        this.clearIntervals()
    }
};

export default SetIntervalMixin;