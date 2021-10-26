import React, {useState} from "react";
import Select from 'react-select';
import Grid from "../../../components/Grid";

// Styles
import styles from './Data.module.scss';

// Images
import uploadIcon from "../../../assets/images/upload.svg";
import Menu from "../../../containers/navs/Menu";

const options = [
    {value: 'chocolate', label: 'Chocolate'},
    {value: 'strawberry', label: 'Strawberry'},
    {value: 'vanilla', label: 'Vanilla'}
]

const Dashboard = () => {
    const [columns,] = useState([
        {name: 'coApp', title: 'CoApp'},
        {name: 'id', title: 'ID'},
        {name: 'role', title: 'Role'},
        {name: 'qualityScore', title: 'Quality Score'},
        {name: 'time', title: 'Time'},
        {name: 'rewards', title: 'Rewards'},
        {name: 'rewardsBalance', title: 'Rewards Balance'}
    ]);

    const [rows,] = useState([
        {
            coApp: 'DMS',
            id: 1008,
            role: 'Provider',
            qualityScore: 8.2,
            time: 'Aug 31, 2021 11:30:15',
            rewards: 196,
            rewardsBalance: 56523
        },
        {
            coApp: 'DMS',
            id: 1007,
            role: 'Provider',
            qualityScore: 7.1,
            time: 'Aug 31, 2021 10:30:15',
            rewards: 175,
            rewardsBalance: 55323
        },
        {
            coApp: 'DMS',
            id: 1006,
            role: 'Provider',
            qualityScore: 9.2,
            time: 'Aug 30, 2021 11:30:15',
            rewards: 253,
            rewardsBalance: 54656
        },
        {
            coApp: 'DMS',
            id: 1005,
            role: 'Provider',
            qualityScore: 2.1,
            time: 'Aug 29, 2021 11:30:58',
            rewards: 45,
            rewardsBalance: 52633
        },
        {
            coApp: 'DMS',
            id: 1004,
            role: 'Provider',
            qualityScore: 0.7,
            time: 'Aug 28, 2021 11:30:15',
            rewards: 12,
            rewardsBalance: 47379
        },
        {
            coApp: 'DMS',
            id: 1003,
            role: 'Provider',
            qualityScore: 5.5,
            time: 'Aug 27, 2021 11:30:15',
            rewards: 95,
            rewardsBalance: 47236
        },
        {
            coApp: 'DMS',
            id: 1002,
            role: 'Provider',
            qualityScore: 6.3,
            time: 'Aug 25, 2021 11:30:15',
            rewards: 120,
            rewardsBalance: 46556
        },
    ]);

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
                                    <p className='bigText mb-0'>13</p>
                                </div>
                            </div>
                            <div className="col-sm-12 col-md-6">
                                <div className="innerCol">
                                    <p>Last Updated</p>
                                    <p className='bigText mb-0'>Aug 31, 2021 11:45:23 AM</p>
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
                                                <Select options={options} className={styles.customSelect}/>
                                            </div>

                                            <div className={styles.previewImgBox}>
                                                <p>Preview</p>
                                                <p className={styles.uploadedfileName}>
                                                    file_xyz_6789.xlsx
                                                </p>
                                            </div>

                                            <div className={styles.submitBtnBox}>
                                                <button name="Submit">Submit</button>
                                            </div>
                                        </div>


                                    </div>
                                    <div className={`col-sm-12 col-md-6`}>
                                        <div className={styles.rightBoxInner}>
                                            <div className="ratio ratio-4x3">
                                                <div className={styles.dragArea}>
                                                    <div className={styles.dragAreaInner}>
                                                        <div className={styles.icon}>
                                                            <img src={uploadIcon} alt="upload file"/>
                                                        </div>
                                                        <header>Drag & Drop to Upload File</header>
                                                        <span>OR</span>
                                                        <button>Browse File</button>
                                                        <input type="file" hidden/>
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
        </>
    )
}

export default Dashboard;
