import React, {useState} from "react";
import Grid from "../../../components/Grid";

// Styles
import styles from './Rewards.module.scss';
import MyResponsivePie from "../../../components/Chart";
import Menu from "../../../containers/navs/Menu";

const data = {
    amountProvided: [
        {
            "id": "",
            "value": 2.5,
            "color": "hsl(0, 100%, 100%)"
        },
        {
            "id": "Amount Provided",
            "value": 7.5,
            "color": "hsl(96, 51%, 68%)"
        }
    ],
    provenanceQuality: [
        {
            "id": "",
            "value": 5.8,
            "color": "hsl(0, 100%, 100%)"
        },
        {
            "id": "Provenance Quality",
            "value": 4.2,
            "color": "hsl(0, 100%, 88%)"
        }
    ],
    dataScarcity: [
        {
            "id": "",
            "value": 4.9,
            "color": "hsl(0, 100%, 100%)"
        },
        {
            "id": "Data Scarcity",
            "value": 5.1,
            "color": "hsl(55, 47%, 63%)"
        }
    ],
    updateFrequency: [
        {
            "id": "",
            "value": 1.8,
            "color": "hsl(0, 100%, 100%)"
        },
        {
            "id": "Update Frequency",
            "value": 8.2,
            "color": "hsl(96, 51%, 68%)"
        }
    ]
};

const Rewards = () => {
    const [columns,] = useState([
        {name: 'id', title: 'ID'},
        {name: 'date', title: 'Date'},
        {name: 'ap', title: 'AP'},
        {name: 'pq', title: 'PQ'},
        {name: 'ds', title: 'DS'},
        {name: 'uf', title: 'UF'},
        {name: 'qualityScore', title: 'Quality Score'},
        {name: 'rewards', title: 'Rewards'}
    ]);

    const [transactionColumns,] = useState([
        {name: 'id', title: 'ID'},
        {name: 'type', title: 'Type'},
        {name: 'rewardsEarned', title: 'Rewards Earned'}
    ]);

    const [transactionRows,] = useState([
        {
            id: 5008,
            type: 'Direct',
            rewardsEarned: 3525
        },
        {
            id: 5007,
            type: 'Indirect',
            rewardsEarned: 264
        },
        {
            id: 5006,
            type: 'Indirect',
            rewardsEarned: 525
        },
        {
            id: 5004,
            type: 'Direct',
            rewardsEarned: 1231
        },
        {
            id: 5005,
            type: 'Indirect',
            rewardsEarned: 234
        },
        {
            id: 5003,
            type: 'Indirect',
            rewardsEarned: 231
        },
        {
            id: 5002,
            type: 'Indirect',
            rewardsEarned: 189
        },
        {
            id: 5001,
            type: 'Direct',
            rewardsEarned: 1232
        }
    ]);

    const [rows,] = useState([
        {
            id: 1008,
            date: '2021-08-28 11:30:18',
            ap: 8.1,
            pq: 8.1,
            ds: 8.1,
            uf: 8.1,
            qualityScore: 8.1,
            rewards: 265
        },
        {
            id: 1007,
            date: '2021-08-28 11:30:18',
            ap: 7.5,
            pq: 7.5,
            ds: 7.5,
            uf: 7.5,
            qualityScore: 7.5,
            rewards: 328
        },
        {
            id: 1006,
            date: '2021-08-28 11:30:18',
            ap: 6.2,
            pq: 6.2,
            ds: 6.2,
            uf: 6.2,
            qualityScore: 6.2,
            rewards: 58
        },
        {
            id: 1004,
            date: '2021-08-28 11:30:18',
            ap: 5.2,
            pq: 5.2,
            ds: 5.2,
            uf: 5.2,
            qualityScore: 5.2,
            rewards: 317
        },
        {
            id: 1005,
            date: '2021-08-28 11:30:18',
            ap: 8.9,
            pq: 8.9,
            ds: 8.9,
            uf: 8.9,
            qualityScore: 8.9,
            rewards: 564
        },
        {
            id: 1003,
            date: '2021-08-28 11:30:18',
            ap: 9.2,
            pq: 9.2,
            ds: 9.2,
            uf: 9.2,
            qualityScore: 9.2,
            rewards: 159
        },
        {
            id: 1002,
            date: '2021-08-28 11:30:18',
            ap: 9.8,
            pq: 9.8,
            ds: 9.8,
            uf: 9.8,
            qualityScore: 9.8,
            rewards: 168
        },
        {
            id: 1001,
            date: '2021-08-28 11:30:18',
            ap: 5.2,
            pq: 5.2,
            ds: 5.2,
            uf: 5.2,
            qualityScore: 5.2,
            rewards: 249
        }
    ]);

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
                                    <p className='bigText mb-0'>56,523</p>
                                </div>
                            </div>
                            <div className="col-sm-12 col-md-4">
                                <div className="innerCol">
                                    <p>Change this Week</p>
                                    <p className='bigText mb-0'>+2,526</p>
                                </div>
                            </div>
                            <div className="col-sm-12 col-md-4">
                                <div className="innerCol">
                                    <p>Quality Score</p>
                                    <p className='bigText mb-0'>7.5</p>
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
                                                        <MyResponsivePie data={data.amountProvided}/>
                                                    </div>
                                                    <div className={styles.counterBox}>
                                                        <p className={styles.counterNumber}>7.5</p>
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
                                                        <MyResponsivePie data={data.provenanceQuality}/>
                                                    </div>
                                                    <div className={styles.counterBox}>
                                                        <p className={styles.counterNumber}>4.2</p>
                                                        <p className={styles.counterText}>Your Score</p>
                                                    </div>
                                                </div>
                                                <div className={styles.reviewTitleContainer}>
                                                    <div className={styles.contentBox}>
                                                        <p className={styles.counterTitle}>Provenance Quality</p>
                                                        <p className={styles.counterAmount}>30%</p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="col-sm-12 col-md-3">
                                            <div className={styles.chartBoxContain}>
                                                <div className={styles.reviewChartContainer}>
                                                    <div className={styles.chartContent}>
                                                        <MyResponsivePie data={data.dataScarcity}/>
                                                    </div>
                                                    <div className={styles.counterBox}>
                                                        <p className={styles.counterNumber}>5.1</p>
                                                        <p className={styles.counterText}>Your Score</p>
                                                    </div>
                                                </div>
                                                <div className={styles.reviewTitleContainer}>
                                                    <div className={styles.contentBox}>
                                                        <p className={styles.counterTitle}>Data Scarcity</p>
                                                        <p className={styles.counterAmount}>30%</p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="col-sm-12 col-md-3">
                                            <div className={styles.chartBoxContain}>
                                                <div className={styles.reviewChartContainer}>
                                                    <div className={styles.chartContent}>
                                                        <MyResponsivePie data={data.updateFrequency}/>
                                                    </div>
                                                    <div className={styles.counterBox}>
                                                        <p className={styles.counterNumber}>8.2</p>
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
                                <h3>Transactions</h3>
                            </div>
                            <div className={`card-body ${styles.transactionCardBody}`}>
                                <div className="row">
                                    <div className="col-sm-12 col-md-6 col-lg-5">
                                        <div className={styles.transactionCol}>
                                            <div className={styles.boxInnerCol}>
                                                <div className={styles.transTitle}>
                                                    <p className="mb-0">Direct Participant</p>
                                                </div>
                                                <div className={styles.transDetail}>
                                                    <div className={`w-50 ${styles.detailText}`}>
                                                        <p className={styles.counterNumber}>3</p>
                                                        <p className={styles.counterText}>Transactions</p>
                                                    </div>
                                                    <div className={`w-50 ${styles.detailText}`}>
                                                        <p className={styles.counterNumber}>12,538</p>
                                                        <p className={styles.counterText}>Rewards Earned</p>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className={styles.boxInnerCol}>
                                                <div className={styles.transTitle}>
                                                    <p className="mb-0">Indirect Participant</p>
                                                </div>
                                                <div className={styles.transDetail}>
                                                    <div className={`w-50 ${styles.detailText}`}>
                                                        <p className={styles.counterNumber}>13</p>
                                                        <p className={styles.counterText}>Transactions</p>
                                                    </div>
                                                    <div className={`w-50 ${styles.detailText}`}>
                                                        <p className={styles.counterNumber}>2,301</p>
                                                        <p className={styles.counterText}>Rewards Earned</p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-sm-12 col-md-6 col-lg-7">
                                        <Grid columns={transactionColumns} rows={transactionRows}/>
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
