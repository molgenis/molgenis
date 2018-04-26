/**
 * This module exposes all the react components.
 * Mainly used for legacy javascript.
 *
 * @module
 */
import React from "react";
import mixin from "./mixin";
import wrapper from "./wrapper";
import Button from "./Button";
import AggregateTable from "./AggregateTable";
import AlertMessage from "./AlertMessage";
import AttributeControl from "./AttributeControl";
import BoolControl from "./BoolControl";
import CheckboxGroup from "./CheckboxGroup";
import CodeEditor from "./CodeEditor";
import DateControl from "./DateControl";
import Dialog from "./Dialog";
import EntitySelectBox from "./EntitySelectBox";
import Form from "./Form";
import FormControl from "./FormControl";
import FormControlGroup from "./FormControlGroup";
import Icon from "./Icon";
import Input from "./Input";
import LanguageSelectBox from "./LanguageSelectBox";
import Modal from "./Modal";
import Pager from "./Pager";
import Popover from "./Popover";
import RadioGroup from "./RadioGroup";
import RangeSlider from "./RangeSlider";
import SelectBox from "./SelectBox";
import Spinner from "./Spinner";
import COL7A1Table from "./COL7A1Table";
import Table from "./Table";
import ConfirmClick from "./ConfirmClick";
import TextArea from "./TextArea";
import ProgressBar from "./ProgressBar";
import ScheduledJobsPlugin from "./ScheduledJobsPlugin";
import jobs from "./jobs";
import UploadContainer from "./UploadContainer";
import UploadForm from "./UploadForm";

// The React components

export default {
    'mixin': mixin,
    'wrapper': wrapper,
    'Button': Button,
    'AggregateTable': AggregateTable,
    'AlertMessage': AlertMessage,
    'AttributeControl': AttributeControl,
    'BoolControl': BoolControl,
    'CheckboxGroup': CheckboxGroup,
    'CodeEditor': CodeEditor,
    'DateControl': DateControl,
    'Dialog': Dialog,
    'EntitySelectBox': EntitySelectBox,
    'Form': Form,
    'FormControl': FormControl,
    'FormControlGroup': FormControlGroup,
    'Icon': Icon,
    'Input': Input,
    'LanguageSelectBox': LanguageSelectBox,
    'Modal': Modal,
    'Pager': Pager,
    'Popover': Popover,
    'RadioGroup': RadioGroup,
    'RangeSlider': RangeSlider,
    'SelectBox': SelectBox,
    'Spinner': Spinner,
    'COL7A1Table': COL7A1Table,
    'Table': Table,
    'TextArea': TextArea,
    'ProgressBar': ProgressBar,
    'jobs': jobs,
    'ScheduledJobsPlugin': ScheduledJobsPlugin,
    'ConfirmClick': ConfirmClick,
    'UploadContainer': UploadContainer,
    'UploadForm': UploadForm
};