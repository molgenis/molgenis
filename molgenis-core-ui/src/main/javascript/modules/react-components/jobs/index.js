import React from 'react'

import Job from "./Job"
import Jobs from "./Jobs"
import JobContainer from "./JobContainer"
import JobsContainer from "./JobsContainer"
import { JobTable, EntityDeleteBtn } from './JobTable'

export {Job, JobContainer, JobsContainer, JobTable, Jobs}

export default {Job, JobContainer, JobsContainer,
    JobTable: React.createFactory(JobTable),
    DeleteBtn: React.createFactory(EntityDeleteBtn), Jobs}