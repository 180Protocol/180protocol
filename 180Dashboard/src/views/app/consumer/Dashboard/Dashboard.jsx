import React, { useEffect, useRef, useState } from "react";
import Grid from "../../../../components/Grid";
import { Field, Form, Formik } from "formik";
import { FormikReactSelect } from "../../../../containers/FormikFields";
import {
    createAggregationRequest,
    fetchDecryptedDataOutput,
    fetchEncryptedDataOutput,
    updateDecentralizedStorageEncryptionKey,
    retrievalDecentralizedStorageEncryptionKey
} from "../../../../store/consumer/actions";
import { useAuthDispatch, useAuthState } from "../../../../store/context";
import { ucWords } from "../../../../utils/helpers";
import moment from "moment";
import AlertBox from "../../../../components/AlertBox";
import { ProgressBar, Step } from "react-step-progress-bar";

// Styles
import styles from './Dashboard.module.scss';
import "react-step-progress-bar/styles.css";

// Images
import downloadIcon from "../../../../assets/images/download.svg";
import selectDownArrow from "../../../../assets/images/select-down-arrow.svg";
import exportIcon from "../../../../assets/images/export.svg";
import refreshIcon from "../../../../assets/images/refresh.svg";

const RightArrowIcon = () => {
    return (
        <div className={styles.IndicatorArrowDiv}>
            <img src={selectDownArrow} className={styles.customSelectArrow} width={20} />
        </div>
    )
}

const steps = ["1", "2", "3", "4"];

const Dashboard = (props) => {
    const dispatch = useAuthDispatch();

    const [columns, setColumns] = useState([]);
    const [encryptedDataOutput, setEncryptedDataOutput] = useState([]);
    const [rows, setRows] = useState([]);
    const [encryptionKey, setEncryptionKey] = useState(null);
    const [lastRequestDate, setLastRequestDate] = useState(null);
    const [step, setStep] = useState(1);
    const alertRef = useRef();

    const dataTypeOptions = localStorage.getItem('dataTypeOptions') ? JSON.parse(localStorage.getItem('dataTypeOptions')) : [];
    const storageTypeOptions = localStorage.getItem('storageTypeOptions') ? JSON.parse(localStorage.getItem('storageTypeOptions')) : [];

    useEffect(() => {
        async function fetchData() {
            return await fetchEncryptedDataOutput(dispatch, props.apiUrl, {});
        }

        async function fetchDecentralizedStorageEncryptionKeyData() {
            return retrievalDecentralizedStorageEncryptionKey(dispatch, props.apiUrl, { "options": { "trackProgress": true } });
        }

        fetchDecentralizedStorageEncryptionKeyData().then((response) => {
            if (response && response.result && response.result.value) {
                setEncryptionKey(response.result.value);
            }
        })

        fetchData().then((response) => {
            getDecryptedData(response);
        });

    }, [dispatch]);

    const getDecryptedData = (response) => {
        setEncryptedDataOutput(response);
        if (response && response.states && response.states.length > 0) {
            let sortedDataOutput = response.states.sort(function (a, b) {
                return new Date(b.state.data.dateCreated) - new Date(a.state.data.dateCreated)
            })
            setLastRequestDate(moment.utc(sortedDataOutput[0].state.data.dateCreated).format("MMM DD, YYYY hh:mm:ss A"));

            getDecryptedDataOutput(response.states[0].state.data.flowTopic, response.states[0].state.data.encryptionKeyId, response.states[0].state.data.storageType, response.states[0].state.data.cid)
        }
    }

    const save = async (values, { resetForm }) => {
        if (step > 3) {
            let params = {
                "options": {
                    "trackProgress": true
                },
                "dataType": values.dataType.value,
                "description": values.description,
                "storageType": values.storageType.value
            };

            let response = await createAggregationRequest(dispatch, props.apiUrl, params);
            if (response) {
                setStep(1);
                resetForm({ values: '' })
                alertRef.current.showAlert('success', 'Request submitted successfully.')
                if (values.storageType.value === 'filecoin') {
                    let storageKeyData = await retrievalDecentralizedStorageEncryptionKey(dispatch, props.apiUrl, { "options": { "trackProgress": true } });
                    if (storageKeyData && storageKeyData.result && storageKeyData.result.value) {
                        setEncryptionKey(storageKeyData.result.value);
                    }
                }
                let res = await fetchEncryptedDataOutput(dispatch, props.apiUrl, {});
                getDecryptedData(res);
            }
        } else {
            next(values.storageType && values.storageType.value === "filecoin" ? true : false);
        }
    }

    const generateDecentralizedStorageEncryptionKey = async () => {
        let params = {
            "options": {
                "trackProgress": true
            }
        };

        let response = await updateDecentralizedStorageEncryptionKey(dispatch, props.apiUrl, params);
        if (response) {
            alertRef.current.showAlert('success', 'Request submitted successfully.');
        }
    }

    const refresh = async () => {
        let response = await fetchEncryptedDataOutput(dispatch, props.apiUrl, {})
        getDecryptedData(response);
    }

    const next = (isStorageTypeFilecoin) => {
        let nextStep = !isStorageTypeFilecoin && step === 2 ? step + 2 : step + 1;
        setStep(nextStep);
    }

    const previous = (isStorageTypeFilecoin) => {
        let previousStep = !isStorageTypeFilecoin && step === 4 ? step - 2 : step - 1;
        setStep(previousStep);
    }

    const validate = (values) => {
        let errors = {};

        if (!values.dataType && step == 1) {
            errors.dataType = "Please select one of data category";
        }

        if (!values.description && step == 4) {
            errors.description = "Please enter description";
        }

        if (!values.storageType && step == 2) {
            errors.storageType = "Please select one of storage type";
        }

        return errors;
    }

    const getDecryptedDataOutput = async (flowTopic, encryptionKeyId, storageType, cid) => {
        let params = {
            "options": {
                "trackProgress": true
            },
            "flowId": flowTopic,
            "storageType": storageType
        }

        if (storageType === "filecoin") {
            params.cid = cid;
            params.encryptionKeyId = encryptionKeyId;
        }

        let decryptedDataOutput = await fetchDecryptedDataOutput(dispatch, props.apiUrl, params);
        let columns = [];
        if (decryptedDataOutput) {
            for (let i = 0; i < decryptedDataOutput.length; i++) {
                for (let property in decryptedDataOutput[i]) {
                    if (columns.length < Object.keys(decryptedDataOutput[i]).length) {
                        columns.push({ name: property, title: ucWords(property) });
                    }
                }
            }
        }

        setColumns(columns);
        setRows(decryptedDataOutput);
    }

    const exportAsCSV = () => {
        let csvData = [];
        csvData.push(Object.keys(rows[0]))
        for (let i = 0; i < rows.length; i++) {
            csvData.push(Object.values(rows[i]));
        }

        let csvContent = "data:text/csv;charset=utf-8,"
            + csvData.map(e => e.join(",")).join("\n");

        let encodedUri = encodeURI(csvContent);
        let link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", "my_data.csv");
        document.body.appendChild(link);

        link.click();
    }

    return (
        <>
            <section className={`${styles.accessData}`}>
                <div className={styles.bgGradient}>
                    <div className={`container ${styles.OverviewContainer}`}>
                        <div className="row">
                            <div className="col-sm-12 col-md-6">
                                <div className="innerCol">
                                    <p>Total Requests</p>
                                    <p className='bigText mb-0'>{encryptedDataOutput && encryptedDataOutput.states && encryptedDataOutput.states.length || 0}</p>
                                </div>
                            </div>
                            <div className="col-sm-12 col-md-6">
                                <div className="innerCol">
                                    <p>Last Request</p>
                                    <p className='bigText mb-0'>{lastRequestDate || '-'}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="outerSpace"></div>
                </div>
                <div className="mainContentSection">
                    <div className={`container mb-5 ${styles.accessDataContainer}`}>
                        <div className="card">
                            <div className="card-header">
                                <h3>Access Data</h3>
                            </div>
                            <div className={`card-body ${styles.accessDataCardBody}`}>
                                <div className={styles.accessDataBodyInner}>
                                    <div className="row" style={{ margin: 5, marginBottom: 20 }}>
                                        <ProgressBar
                                            width={500}
                                            percent={100 * ((step - 1) / (steps.length - 1)) - 1}
                                            filledBackground="linear-gradient(to right, #35607e, #1e303e)"
                                        >
                                            {steps.map((step, index, arr) => {
                                                return (
                                                    <Step
                                                        transition="scale"
                                                        children={({ accomplished }) => (
                                                            <div
                                                                style={{
                                                                    display: "flex",
                                                                    alignItems: "center",
                                                                    justifyContent: "center",
                                                                    borderRadius: "50%",
                                                                    width: 20,
                                                                    height: 20,
                                                                    color: "black",
                                                                    backgroundColor: accomplished ? "#35607e" : "gray"
                                                                }}
                                                            >
                                                                <br />
                                                                <br />
                                                                <br />
                                                                {step}
                                                            </div>
                                                        )}
                                                    />
                                                );
                                            })}
                                        </ProgressBar>
                                    </div>
                                    <div className="row">
                                        <Formik
                                            validate={validate}
                                            initialValues={{
                                                dataType: "",
                                                description: "",
                                                storageType: ""
                                            }}
                                            onSubmit={save}
                                        >
                                            {({ errors, touched, values, setFieldValue, setFieldTouched }) => (
                                                <Form className='auth-form'>
                                                    <div className="col-sm-12 col-md-12">
                                                        <div className={styles.accessDataBoxInner}>
                                                            {
                                                                step === 1 ?
                                                                    <div className={styles.selectCateBox}>
                                                                        <p>Data Category</p>
                                                                        <FormikReactSelect
                                                                            className={styles.customSelect}
                                                                            name="dataType"
                                                                            id="dataType"
                                                                            value={values.dataType}
                                                                            isMulti={false}
                                                                            options={dataTypeOptions}
                                                                            onChange={setFieldValue}
                                                                            onBlur={setFieldTouched}
                                                                            components={{
                                                                                DropdownIndicator: RightArrowIcon,
                                                                                IndicatorSeparator: () => null
                                                                            }}
                                                                        />
                                                                        {errors.dataType && touched.dataType &&
                                                                            <div
                                                                                className="invalid-feedback-msg">{errors.dataType}</div>}
                                                                    </div> : null
                                                            }
                                                            {
                                                                step === 2 ?
                                                                    <div className={styles.selectCateBox}>
                                                                        <p>Storage Type</p>
                                                                        <FormikReactSelect
                                                                            className={styles.customSelect}
                                                                            name="storageType" id="storageType"
                                                                            value={values.storageType}
                                                                            isMulti={false}
                                                                            options={storageTypeOptions}
                                                                            onChange={setFieldValue}
                                                                            onBlur={setFieldTouched}
                                                                            components={{
                                                                                DropdownIndicator: RightArrowIcon,
                                                                                IndicatorSeparator: () => null
                                                                            }}
                                                                        />
                                                                        {errors.storageType && touched.storageType &&
                                                                            <div
                                                                                className="invalid-feedback-msg">{errors.storageType}</div>}
                                                                    </div> : null
                                                            }
                                                            {
                                                                step === 3 ?
                                                                    <div className={styles.submitBoxInner}>
                                                                        <div className={styles.submitBtnBox}>
                                                                            <button type="button" name="Generate"
                                                                                onClick={generateDecentralizedStorageEncryptionKey}>{encryptionKey ? "Update " : "Generate "}
                                                                                Key
                                                                            </button>
                                                                            <p>{encryptionKey ? "You have already generated a data encryption key (DEK). If you want to update the DEK then click on update key." : "You have to generate a data encryption key (DEK) to encrypt data for storage on Filecoin network."}</p>
                                                                        </div>
                                                                    </div> : null
                                                            }
                                                            {
                                                                step === 4 ?
                                                                    <div className={styles.descriptionCateBox}>
                                                                        <p>Description</p>
                                                                        <div>
                                                                            <Field name="description"
                                                                                className={styles.inputFormControl} />
                                                                            {errors.description && touched.description &&
                                                                                <div
                                                                                    className="invalid-feedback-msg">{errors.description}</div>}
                                                                        </div>
                                                                    </div> : null
                                                            }
                                                        </div>
                                                    </div>
                                                    <div className={`col-sm-5 col-md-5`}>
                                                        <div className={styles.submitBoxInner}>
                                                            {
                                                                step !== 1 ?
                                                                    <div className={styles.submitBtnBox} style={{ float: 'left' }}>
                                                                        <button type="button" name="Previous" onClick={() => previous(values.storageType.value === "filecoin" ? true : false)}>Previous</button>
                                                                    </div> : null
                                                            }
                                                            <div className={styles.submitBtnBox} style={{ float: 'right' }}>
                                                                <button type="submit" name="Next">{step === 4 ? 'Submit' : 'Next'}</button>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </Form>
                                            )}
                                        </Formik>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className={`container mb-5 ${styles.requestsContainer}`}>
                        <div className="card">
                            <div className={`card-header ${styles.requestsCardHeader}`}>
                                <h3>Requests</h3>
                                <div className={styles.refreshContainer}>
                                    <p>Use refresh button to load latest results</p>
                                    <button type="button" name="Refresh"
                                        onClick={refresh}>REFRESH <img
                                            src={refreshIcon} alt="refresh" />
                                    </button>
                                </div>
                            </div>
                            <div className={`card-body ${styles.requestsCardBody}`}>
                                <div className={styles.requestsBodyInner}>
                                    <div className={styles.requestsBoxInner}>
                                        <div className="row five-col">
                                            {
                                                encryptedDataOutput && encryptedDataOutput.states && encryptedDataOutput.states.length > 0 ?
                                                    encryptedDataOutput.states.map((output, index) => {
                                                        return (
                                                            <div key={index}
                                                                onClick={() => getDecryptedDataOutput(output.state.data.flowTopic, output.state.data.encryptionKeyId, output.state.data.storageType, output.state.data.cid)}
                                                                className="col-sm-12 col-md-3 col-lg-4 col-xl-2">
                                                                <div className={styles.downloadRequestBox}>
                                                                    <img src={downloadIcon} alt="download" />
                                                                    <div className={styles.requestInfoBox}>
                                                                        <p>{output.state.data.dataType}</p>
                                                                        <p>{output.state.data.description}</p>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        );
                                                    }) : <p>No completed requests at this time.</p>
                                            }
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    {
                        rows && rows.length > 0 ?
                            <div className={`container mb-5 ${styles.exportDataContainer}`}>
                                <div className="card">
                                    <div className={`card-header ${styles.exportCardHeader}`}>
                                        <h3>Preview</h3>
                                        <button onClick={exportAsCSV}>EXPORT <img src={exportIcon}
                                            className={styles.exportbtnIcon}
                                            alt="export" />
                                        </button>
                                    </div>
                                    <div className={`card-body ${styles.previewTableCardBody}`}>
                                        <div className={styles.previewTableBodyInner}>
                                            <Grid className={styles.aggregationsTable} columns={columns} rows={rows} />
                                        </div>
                                    </div>
                                </div>
                            </div> : null
                    }
                </div>
            </section>
            <AlertBox ref={alertRef} />
        </>
    )
}

export default Dashboard;
