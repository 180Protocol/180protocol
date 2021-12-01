import React, {useEffect, useState} from "react";
import Grid from "../../../../components/Grid";
import MyResponsivePie from "../../../../components/Chart";
import Menu from "../../../../containers/navs/Menu";
import {fetchDecryptedRewardsData, fetchEncryptedRewardsData} from "../../../../store/provider/actions";
import {useAuthDispatch} from "../../../../store/context";
import {average, sum} from "../../../../utils/helpers";

// Styles
import styles from './Rewards.module.scss';
import moment from "moment";

const Rewards = () => {
    const dispatch = useAuthDispatch();

    const [columns,] = useState([
        {name: 'id', title: 'ID'},
        {name: 'date', title: 'Date'},
        {name: 'amountProvided', title: 'AP'},
        {name: 'completeness', title: 'Completeness'},
        {name: 'uniqueness', title: 'Uniqueness'},
        {name: 'updateFrequency', title: 'UF'},
        {name: 'qualityScore', title: 'Quality Score'},
        {name: 'rewards', title: 'Rewards'}
    ]);

    const [rows, setRows] = useState([]);
    const [changeThisWeek, setChangeThisWeek] = useState(0);

    const [options, setOptions] = useState({
        amountProvided: [],
        completeness: [],
        uniqueness: [],
        updateFrequency: []
    });

    useEffect(() => {
        async function fetchData() {
            return await fetchEncryptedRewardsData(dispatch, {});
        }

        fetchData().then(async (response) => {
            if (response && response.states && response.states.length > 0) {
                let rewardsData = response.states.map((item) => {
                    return {
                        "rewards": item.state.data.rewards,
                        "flowTopic": item.state.data.flowTopic
                    }
                });

                let params = {
                    "options": {
                        "trackProgress": "true"
                    },
                    "rewardsData": rewardsData
                }

                let decryptedRewardsData = await fetchDecryptedRewardsData(dispatch, params)
                setRows(decryptedRewardsData.result.value);
                let todayRewards = decryptedRewardsData.result.value.find((item) => {
                    return moment.utc(item.date).isSame(moment().toISOString(), 'day')
                });

                let oneWeekBeforeRewards = decryptedRewardsData.result.value.find((item) => {
                    return moment.utc(item.date).isSame(moment().subtract(7, 'd').toISOString(), 'day')
                });

                if (todayRewards && oneWeekBeforeRewards) {
                    setChangeThisWeek(todayRewards.rewards - oneWeekBeforeRewards.rewards);
                }

                setOptions({
                    amountProvided: [
                        {
                            "id": "",
                            "value": 10 - parseFloat(average(decryptedRewardsData.result.value, "amountProvided")),
                            "color": "hsl(0, 100%, 100%)"
                        },
                        {
                            "id": "Amount Provided",
                            "value": parseFloat(average(decryptedRewardsData.result.value, "amountProvided")),
                            "color": "hsl(96, 51%, 68%)"
                        }
                    ],
                    completeness: [
                        {
                            "id": "",
                            "value": 10 - parseFloat(average(decryptedRewardsData.result.value, "completeness")),
                            "color": "hsl(0, 100%, 100%)"
                        },
                        {
                            "id": "Completeness",
                            "value": parseFloat(average(decryptedRewardsData.result.value, "completeness")),
                            "color": "hsl(0, 100%, 88%)"
                        }
                    ],
                    uniqueness: [
                        {
                            "id": "",
                            "value": 10 - parseFloat(average(decryptedRewardsData.result.value, "uniqueness")),
                            "color": "hsl(0, 100%, 100%)"
                        },
                        {
                            "id": "Uniqueness",
                            "value": parseFloat(average(decryptedRewardsData.result.value, "uniqueness")),
                            "color": "hsl(55, 47%, 63%)"
                        }
                    ],
                    updateFrequency: [
                        {
                            "id": "",
                            "value": 10 - parseFloat(average(decryptedRewardsData.result.value, "updateFrequency")),
                            "color": "hsl(0, 100%, 100%)"
                        },
                        {
                            "id": "Update Frequency",
                            "value": parseFloat(average(decryptedRewardsData.result.value, "updateFrequency")),
                            "color": "hsl(96, 51%, 68%)"
                        }
                    ]
                });
            }
        });
    }, [dispatch]);

    return (
        <>
            <section className={`${styles.Reward}`}>
                <div className={styles.bgGradient}>
                    <Menu/>
                    <div className={`container ${styles.OverviewContainer}`}>
                        <div className="row">
                            <div className="col-sm-12 col-md-4">
                                <div className="innerCol">
                                    <p>Rewards Balance</p>
                                    <p className='bigText mb-0'>{rows && rows.length > 0 ? Intl.NumberFormat().format(sum(rows, "rewards")) : 0}</p>
                                </div>
                            </div>
                            <div className="col-sm-12 col-md-4">
                                <div className="innerCol">
                                    <p>Change this Week</p>
                                    <p className='bigText mb-0'>{Intl.NumberFormat().format(changeThisWeek.toFixed(1))}</p>
                                </div>
                            </div>
                            <div className="col-sm-12 col-md-4">
                                <div className="innerCol">
                                    <p>Quality Score</p>
                                    <p className='bigText mb-0'>{rows && rows.length > 0 ? average(rows, "qualityScore") : 0}</p>
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
                                <h3>Aggregation Rewards</h3>
                            </div>
                            <div className={`card-body ${styles.aggregationCardBody}`}>
                                <div className='container mb-5'>
                                    <div className="row">
                                        <div className="col-sm-12 col-md-3">
                                            <div className={styles.chartBoxContain}>
                                                <div className={styles.reviewChartContainer}>
                                                    <div className={styles.chartContent}>
                                                        <MyResponsivePie data={options.amountProvided}/>
                                                    </div>
                                                    <div className={styles.counterBox}>
                                                        <p className={styles.counterNumber}>{rows && rows.length > 0 ? average(rows, "amountProvided") : 0}</p>
                                                        <p className={styles.counterText}>Your Score</p>
                                                    </div>
                                                </div>
                                                <div className={styles.reviewTitleContainer}>
                                                    <div className={styles.contentBox}>
                                                        <p className={styles.counterTitle}>Amount Provided</p>
                                                        <p className={styles.counterAmount}>30%</p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="col-sm-12 col-md-3">
                                            <div className={styles.chartBoxContain}>
                                                <div className={styles.reviewChartContainer}>
                                                    <div className={styles.chartContent}>
                                                        <MyResponsivePie data={options.completeness}/>
                                                    </div>
                                                    <div className={styles.counterBox}>
                                                        <p className={styles.counterNumber}>{average(rows, "completeness")}</p>
                                                        <p className={styles.counterText}>Your Score</p>
                                                    </div>
                                                </div>
                                                <div className={styles.reviewTitleContainer}>
                                                    <div className={styles.contentBox}>
                                                        <p className={styles.counterTitle}>Completeness</p>
                                                        <p className={styles.counterAmount}>30%</p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="col-sm-12 col-md-3">
                                            <div className={styles.chartBoxContain}>
                                                <div className={styles.reviewChartContainer}>
                                                    <div className={styles.chartContent}>
                                                        <MyResponsivePie data={options.uniqueness}/>
                                                    </div>
                                                    <div className={styles.counterBox}>
                                                        <p className={styles.counterNumber}>{average(rows, "uniqueness")}</p>
                                                        <p className={styles.counterText}>Your Score</p>
                                                    </div>
                                                </div>
                                                <div className={styles.reviewTitleContainer}>
                                                    <div className={styles.contentBox}>
                                                        <p className={styles.counterTitle}>Uniqueness</p>
                                                        <p className={styles.counterAmount}>30%</p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="col-sm-12 col-md-3">
                                            <div className={styles.chartBoxContain}>
                                                <div className={styles.reviewChartContainer}>
                                                    <div className={styles.chartContent}>
                                                        <MyResponsivePie data={options.updateFrequency}/>
                                                    </div>
                                                    <div className={styles.counterBox}>
                                                        <p className={styles.counterNumber}>{average(rows, "updateFrequency")}</p>
                                                        <p className={styles.counterText}>Your Score</p>
                                                    </div>
                                                </div>
                                                <div className={styles.reviewTitleContainer}>
                                                    <div className={styles.contentBox}>
                                                        <p className={styles.counterTitle}>Update Frequency</p>
                                                        <p className={styles.counterAmount}>10%</p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div className="row" style={{marginTop: 20}}>
                                    <Grid columns={columns} rows={rows}/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="container mb-5">
                        <div className="card">
                            <div className="card-header">
                                <h3>Rewards Events</h3>
                            </div>
                            <div className={`card-body ${styles.transactionCardBody}`}>
                                <div className="row">
                                    <div className="col-sm-12 col-md-6 col-lg-5">
                                        <p>Coming Soon</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </>
    )
}

export default Rewards;
