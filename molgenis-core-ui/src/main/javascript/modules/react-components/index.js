/**
 * This module exposes all the react components.
 * Mainly used for legacy javascript.
 * 
 * @module
 */
import mixin from './mixin';
import wrapper from './wrapper';

// The React components
import Button from './Button';
import AggregateTable from './AggregateTable';
import AlertMessage from './AlertMessage';
import AttributeControl from './AttributeControl';
import BoolControl from './BoolControl';
import CheckboxGroup from './CheckboxGroup';
import CodeEditor from './CodeEditor';
import DateControl from './DateControl';
import Dialog from './Dialog';
import EntitySelectBox from './EntitySelectBox';
import Form from './Form';
import FormControl from './FormControl';
import FormControlGroup from './FormControlGroup';
import Icon from './Icon';
import Input from './Input';
import LanguageSelectBox from './LanguageSelectBox';
import Modal from './Modal';
import Pager from './Pager';
import Popover from './Popover';
import Questionnaire from './Questionnaire';
import RadioGroup from './RadioGroup';
import RangeSlider from './RangeSlider';
import SelectBox from './SelectBox';
import Spinner from './Spinner';
import Table from './Table';
import TextArea from './TextArea';
import ProgressBar from './ProgressBar';

export default {
	'mixin' : mixin,
	'wrapper' : wrapper,
	'Button' : Button,
	'AggregateTable' : AggregateTable,
	'AlertMessage' : AlertMessage,
	'AttributeControl' : AttributeControl,
	'BoolControl' : BoolControl,
	'CheckboxGroup' : CheckboxGroup,
	'CodeEditor' : CodeEditor,
	'DateControl' : DateControl,
	'Dialog' : Dialog,
	'EntitySelectBox' : EntitySelectBox,
	'Form' : Form,
	'FormControl' : FormControl,
	'FormControlGroup' : FormControlGroup,
	'Icon' : Icon,
	'Input' : Input,
	'LanguageSelectBox' : LanguageSelectBox,
	'Modal' : Modal,
	'Pager' : Pager,
	'Popover' : Popover,
	'Questionnaire' : Questionnaire,
	'RadioGroup' : RadioGroup,
	'RangeSlider' : RangeSlider,
	'SelectBox' : SelectBox,
	'Spinner' : Spinner,
	'Table' : Table,
	'TextArea' : TextArea,
	'ProgressBar' : ProgressBar
};