import "jquery";
import "bootstrap";
import "underscore";
import "jquery-ui";
import "react-components/css/wrapper/jquery-ui-1.9.2.custom.min.css";
import "jquery.cookie";
import "brace";
import "moment";
import "eonasdan-bootstrap-datetimepicker";
import "eonasdan-bootstrap-datetimepicker/build/css/bootstrap-datetimepicker.css";
import "jq-edit-rangeslider";
import "react-components/css/wrapper/JQRangeSlider.css";
import "select2";
import "react-components/css/wrapper/select2.css";
import "urijs";
import "react";
import "react/addons"
import "promise";

let mode = 'r';
let theme = 'eclipse';

require("brace/mode/"+mode);
require("brace/theme/"+theme);