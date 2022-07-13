import React, { useState, useCallback, useEffect, useRef } from "react";
import Select from 'react-select';
import Grid from "../../../../components/Grid";
import { useDropzone } from "react-dropzone";
import Menu from "../../../../containers/navs/Menu";
import { useAuthDispatch, useAuthState } from "../../../../store/context";
import {
    fetchDecryptedRewardsData, fetchEncryptedRewardsData, upload,
    updateDecentralizedStorageEncryptionKey,
    retrievalDecentralizedStorageEncryptionKey
} from "../../../../store/provider/actions";
import AlertBox from "../../../../components/AlertBox";
import { ProgressBar, Step } from "react-step-progress-bar";
import { Field, Form, Formik } from "formik";
import { FormikReactSelect } from "../../../../containers/FormikFields";
import moment from "moment";

// Styles
import styles from './Data.module.scss';

// Images
import uploadIcon from "../../../../assets/images/upload.svg";
import selectDownArrow from "../../../../assets/images/select-down-arrow.svg";

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
    const userDetails = useAuthState();
    const alertRef = useRef();

    const [columns,] = useState([
        { name: 'coApp', title: 'CoApp' },
        { name: 'flowId', title: 'ID' },
        { name: 'qualityScore', title: 'Quality Score' },
        { name: 'date', title: 'Date' },
        { name: 'rewards', title: 'Rewards' },
        { name: 'rewardsBalance', title: 'Rewards Balance' }
    ]);

    const [rows, setRows] = useState([]);
    const [lastUpdated, setLastUpdated] = useState(null);

    const [selectedFiles, setSelectedFiles] = useState([]);
    const [dataType, setDataType] = useState({});
    const [encryptionKey, setEncryptionKey] = useState(null);
    const [step, setStep] = useState(1);

    const dataTypeOptions = localStorage.getItem('dataTypeOptions') ? JSON.parse(localStorage.getItem('dataTypeOptions')) : [];
    const storageTypeOptions = localStorage.getItem('storageTypeOptions') ? JSON.parse(localStorage.getItem('storageTypeOptions')) : [];

    useEffect(() => {
        async function fetchData() {
            return await fetchEncryptedRewardsData(dispatch, props.apiUrl, {});
        }

        async function fetchDecentralizedStorageEncryptionKeyData() {
            return retrievalDecentralizedStorageEncryptionKey(dispatch, props.apiUrl, { "options": { "trackProgress": true } });
        }

        fetchDecentralizedStorageEncryptionKeyData().then((response) => {
            if (response && response.result && response.result.value) {
                setEncryptionKey(response.result.value);
            }
        })

        fetchData().then(async (response) => {
            if (response && response.states && response.states.length > 0) {
                const promises = getDecryptedData(response.states);
                let decryptedRewardsData = [];
                Promise.all(promises).then(values => {
                    for (let i = 0, len = values.length; i < len; i++) {
                        decryptedRewardsData.push(Object.assign({}, values[i][0]));
                    }

                    let sum = 0;
                    for (let i = 0; i < decryptedRewardsData.length; i++) {
                        sum += parseFloat(decryptedRewardsData[i].rewards);
                        decryptedRewardsData[i].rewardsBalance = sum;
                    }

                    setRows(decryptedRewardsData);
                    let sortedRewardsData = decryptedRewardsData.sort(function (a, b) {
                        return new Date(b.date) - new Date(a.date)
                    })

                    setLastUpdated(moment.utc(sortedRewardsData[0].date).format("MMM DD, YYYY hh:mm:ss A"));
                });
            }
        });
    }, [dispatch]);

    const getDecryptedData = (states) => {
        return states.map(async (option) => {
            let params = {
                "options": {
                    "trackProgress": true
                },
                "flowId": option.state.data.flowTopic
            }

            let decryptedRewardsData = await fetchDecryptedRewardsData(dispatch, props.apiUrl, params, option.state.data.dateCreated)
            return decryptedRewardsData;
        });
    }

    const onDrop = useCallback((acceptedFiles) => {
        setSelectedFiles(acceptedFiles);
    }, []);

    const { getRootProps, getInputProps } = useDropzone({ onDrop })

    const handleChange = (val) => {
        setDataType(val);
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

        if (selectedFiles.length === 0 && step == 4) {
            errors.description = "Please enter description";
        }

        if (!values.storageType && step == 2) {
            errors.storageType = "Please select one of storage type";
        }

        return errors;
    }

    const save = async (values, { resetForm }) => {
        if (step > 3) {
            let formData = new FormData();
            formData.append("file", selectedFiles[0]);
            formData.append("dataType", values.dataType.value);
            formData.append("storageType", values.storageType.value);
            formData.append("encryptionKeyId", encryptionKey);

            let response = await upload(dispatch, props.apiUrl, formData, values.storageType.value);
            if (response) {
                setStep(1);
                resetForm({ values: '' });
                setSelectedFiles([]);
                alertRef.current.showAlert('success', 'Data uploaded successfully.')
                if (values.storageType.value === 'filecoin') {
                    let storageKeyData = await retrievalDecentralizedStorageEncryptionKey(dispatch, props.apiUrl, { "options": { "trackProgress": true } });
                    if (storageKeyData && storageKeyData.result && storageKeyData.result.value) {
                        setEncryptionKey(storageKeyData.result.value);
                    }
                }
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

    return (
        <>
            <section className={`${styles.dashboard}`}>
                <div className={styles.bgGradient}>
                    <Menu />
                    <div className={`container ${styles.OverviewContainer}`}>
                        <div className="row">
                            <div className="col-sm-12 col-md-6">
                                <div className="innerCol">
                                    <p>Total Aggregations</p>
                                    <p className='bigText mb-0'>{rows.length}</p>
                                </div>
                            </div>
                            <div className="col-sm-12 col-md-6">
                                <div className="innerCol">
                                    <p>Last Updated</p>
                                    <p className='bigText mb-0'>{lastUpdated}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="outerSpace"></div>
                </div>
                <div className="mainContentSection">
                    <div className="container mb-5">
                        <div className="card align-items-center">
                            <div className="card-header">
                                <h3>Upload Data</h3>
                            </div>
                            <div className={`card-body ${styles.dataUploadCardBody}`}>
                                <div className="row" style={{ margin: 5, marginBottom: 60 }}>
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
                                            storageType: ""
                                        }}
                                        onSubmit={save}
                                    >
                                        {({ errors, touched, values, setFieldValue, setFieldTouched }) => (
                                            <Form className='auth-form'>
                                                <div className="col-sm-12 col-md-12">
                                                    <div className={styles.leftBoxInner}>
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
                                                                        {
                                                                            encryptionKey ?
                                                                                <>
                                                                                    <p>You have already generated a data encryption key (DEK).</p>
                                                                                    <p>If you want to update the DEK then click on update key.</p>
                                                                                </>
                                                                                :
                                                                                <>
                                                                                    <p>You have to generate a data encryption key (DEK)</p>
                                                                                    <p>to encrypt data for storage on Filecoin network.</p>
                                                                                </>
                                                                        }
                                                                    </div>
                                                                </div> : null
                                                        }
                                                        {
                                                            step === 4 ?
                                                                <div className="row">
                                                                    <div className={`col-sm-12 col-md-12`}>
                                                                        <div className={styles.rightBoxInner}>
                                                                            <div>
                                                                                <div className={styles.dragArea} {...getRootProps()}>
                                                                                    <div className={styles.dragAreaInner}>
                                                                                        <div className={styles.icon}>
                                                                                            <img src={uploadIcon} alt="upload file" />
                                                                                        </div>
                                                                                        <header>Drag & Drop to Upload File
                                                                                                OR &nbsp;&nbsp; 
                                                                                            <button>Browse File</button>
                                                                                        </header>
                                                                                        <p className={styles.uploadedfileName}>
                                                                                {selectedFiles && selectedFiles.length > 0 ?
                                                                                    selectedFiles.map((file, key) => {
                                                                                        return file.name
                                                                                    }) : ''}
                                                                            </p>
                                                                                        <input {...getInputProps()} />
                                                                                    </div>
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </div> : null
                                                        }
                                                    </div>
                                                </div>
                                                <div className={`col-sm-12 col-md-12`}>
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
                    <div className="container">
                        <div className="card gridTableCard">
                            <div className="card-header">
                                <h3>Aggregations</h3>
                            </div>
                            <div className={`card-body ${styles.aggregTableCardBody}`}>
                                <Grid className={styles.aggregationsTable} columns={columns} rows={rows} />
                            </div>
                        </div>
                    </div>
                </div>
            </section>
            <AlertBox ref={alertRef} />
        </>
    )
}

export default Dashboard;
