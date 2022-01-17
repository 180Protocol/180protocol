import React, {useState, useCallback, useEffect, useRef} from "react";
import Select from 'react-select';
import Grid from "../../../../components/Grid";
import {useDropzone} from "react-dropzone";
import Menu from "../../../../containers/navs/Menu";
import {useAuthDispatch, useAuthState} from "../../../../store/context";
import {fetchDecryptedRewardsData, fetchEncryptedRewardsData, upload} from "../../../../store/provider/actions";
import AlertBox from "../../../../components/AlertBox";
// Styles
import styles from './Data.module.scss';

// Images
import uploadIcon from "../../../../assets/images/upload.svg";
import moment from "moment";

const Dashboard = (props) => {
    const dispatch = useAuthDispatch();
    const userDetails = useAuthState();
    const alertRef = useRef();

    const [columns,] = useState([
        {name: 'coApplication', title: 'CoApp'},
        {name: 'id', title: 'ID'},
        {name: 'qualityScore', title: 'Quality Score'},
        {name: 'date', title: 'Time'},
        {name: 'rewards', title: 'Rewards'},
        {name: 'rewardsBalance', title: 'Rewards Balance'}
    ]);

    const [rows, setRows] = useState([]);
    const [lastUpdated, setLastUpdated] = useState(null);

    const [selectedFiles, setSelectedFiles] = useState([]);
    const [dataType, setDataType] = useState({});

    const dataTypeOptions = localStorage.getItem('dataTypeOptions') ? JSON.parse(localStorage.getItem('dataTypeOptions')) : [];

    useEffect(() => {
        async function fetchData() {
            return await fetchEncryptedRewardsData(dispatch, props.apiUrl, {});
        }

        fetchData().then(async (response) => {
            if (response && response.states && response.states.length > 0) {
                let params = {
                    "options": {
                        "trackProgress": true
                    },
                    "flowId": response.states[0].state.data.flowTopic
                }

                let decryptedRewardsData = await fetchDecryptedRewardsData(dispatch, props.apiUrl, params)
                setRows(decryptedRewardsData);
                let sortedRewardsData = decryptedRewardsData.sort(function (a, b) {
                    return new Date(b.date) - new Date(a.date)
                })

                setLastUpdated(moment.utc(sortedRewardsData[0].date).format("MMM DD, YYYY hh:mm:ss A"));
            }
        });
    }, [dispatch]);

    const onDrop = useCallback((acceptedFiles) => {
        setSelectedFiles(acceptedFiles);
    }, []);

    const {getRootProps, getInputProps} = useDropzone({onDrop})

    const handleChange = (val) => {
        setDataType(val);
    }

    const uploadData = async () => {
        let formData = new FormData();
        formData.append("data", selectedFiles[0]);
        formData.append("dataType", dataType.value);
        formData.append("uploader", userDetails.user.username);
        formData.append("filename", selectedFiles[0].name);

        let response = await upload(dispatch, props.apiUrl, formData);
        if (response) {
            alertRef.current.showAlert('success', 'Data uploaded successfully.')
        }
    }

    return (
        <>
            <section className={`${styles.dashboard}`}>
                <div className={styles.bgGradient}>
                    <Menu/>
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
                        <div className="card">
                            <div className="card-header">
                                <h3>Upload Data</h3>
                            </div>
                            <div className={`card-body ${styles.dataUploadCardBody}`}>
                                <div className="row">
                                    <div className="col-sm-12 col-md-6">
                                        <div className={styles.leftBoxInner}>

                                            <div className={styles.selectCateBox}>
                                                <p>Data Category</p>
                                                <Select defaultValue={dataType} options={dataTypeOptions}
                                                        onChange={handleChange} className={styles.customSelect}/>
                                            </div>

                                            <div className={styles.previewImgBox}>
                                                <p>Preview</p>
                                                <p className={styles.uploadedfileName}>
                                                    {selectedFiles && selectedFiles.length > 0 ?
                                                        selectedFiles.map((file, key) => {
                                                            return file.name
                                                        }) : 'Please select file to preview'}
                                                </p>
                                            </div>

                                            <div className={styles.submitBtnBox}>
                                                <button name="Submit" type="button" onClick={uploadData}>Submit</button>
                                            </div>
                                        </div>


                                    </div>
                                    <div className={`col-sm-12 col-md-6`}>
                                        <div className={styles.rightBoxInner}>
                                            <div className="ratio ratio-4x3">
                                                <div className={styles.dragArea} {...getRootProps()}>
                                                    <div className={styles.dragAreaInner}>
                                                        <div className={styles.icon}>
                                                            <img src={uploadIcon} alt="upload file"/>
                                                        </div>
                                                        <header>Drag & Drop to Upload File</header>
                                                        <span>OR</span>
                                                        <button>Browse File</button>
                                                        <input {...getInputProps()} />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
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
                                <Grid className={styles.aggregationsTable} columns={columns} rows={rows}/>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
            <AlertBox ref={alertRef}/>
        </>
    )
}

export default Dashboard;
