import "jquery";
import "jq-edit-rangeslider";
import "jquery-ui";
import "jquery.cookie";
import "select2";
import "bootstrap";
import "eonasdan-bootstrap-datetimepicker";
import "react";
import "react/addons"
import "underscore";
import "brace";
import "moment";
import "urijs";
import "promise";

import "eonasdan-bootstrap-datetimepicker/build/css/bootstrap-datetimepicker.css";
import "react-components/css/wrapper/select2.css";
import "react-components/css/wrapper/jquery-ui-1.9.2.custom.min.css";
import "react-components/css/wrapper/JQRangeSlider.css";

let mode = 'r';
let theme = 'eclipse';

require("brace/mode/"+mode);
require("brace/theme/"+theme);