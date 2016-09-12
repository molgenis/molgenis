/**
 * This module exposes all the wrapper classes.
 * Mainly used for legacy javascript.
 *
 * @module wrapper
 */

import Ace from "./Ace";
import DateTimePicker from "./DateTimePicker";
import JQRangeSlider from "./JQRangeSlider";
import JQueryForm from "./JQueryForm";
import Select2 from "./Select2";

var wrapper = {
    'Ace': Ace,
    'DateTimePicker': DateTimePicker,
    'JQRangeSlider': JQRangeSlider,
    'JQueryForm': JQueryForm,
    'Select2': Select2
};

export default wrapper;