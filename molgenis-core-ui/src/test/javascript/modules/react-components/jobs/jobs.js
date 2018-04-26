const job_success = {
    identifier: "test_1",
    status: "SUCCESS",
    type: "TEST job",
    entityTypeId: "TapeTest",
    progressMax: 1000,
    progressInt: 1000,
    progressMessage: "Test (started by Tape)",
    submissionDate: "2100-01-01T00:00:00+0100",
    startDate: "2100-01-01T00:15:00+0100",
    endDate: "2100-01-01T00:30:00+0100",
};

const job_running = {
    identifier: "test_2",
    status: "RUNNING",
    type: "TEST job",
    entityTypeId: "TapeTest",
    progressMax: 500,
    progressInt: 100,
    progressMessage: "Test (started by Tape)",
    submissionDate: "2200-01-01T00:00:00+0100",
    startDate: "2200-01-01T00:15:00+0100",
    endDate: "2200-01-01T00:30:00+0100",
}

const job_failed = {
    identifier: "test_3",
    status: "FAILED",
    type: "TEST job",
    entityTypeId: "TapeTest",
    progressMax: 200,
    progressInt: 50,
    progressMessage: "Test (started by Tape)",
    submissionDate: "2300-01-01T00:00:00+0100",
    startDate: "2300-01-01T00:15:00+0100",
    endDate: "2300-01-01T00:30:00+0100",
}

const jobs = [job_success, job_running, job_failed];

export {job_failed, job_running, job_success, jobs}