import {createServer} from "miragejs"

export function makeServer({environment = "development"} = {}) {
    let server = createServer({
        environment,

        routes() {
            this.get("/180Protocol Broker Contracts/DataOutputState/query", () => {
                return {
                    "stateTypes": "UNCONSUMED",
                    "states": [
                        {
                            "ref": {
                                "index": 0,
                                "txhash": "384EFB8B90124712DBC8F0BD61D8D1120D1A4AA21F6F9D187856B1684BD61FDC"
                            },
                            "state": {
                                "constraint": {
                                    "type": "signature",
                                    "key": {
                                        "hash": "CTymWiNxvGWE117QUKSjeW71yaaGc5tNL2i8Zp8dFUvE"
                                    }
                                },
                                "contract": "com.protocol180.aggregator.contract.DataOutputContract",
                                "data": {
                                    "consumer": {
                                        "owningKey": {
                                            "hash": "CUK712EteZBrQMZ3ecgo6ybqYjWCGyb64fMQ1RjM3wvW"
                                        }
                                    },
                                    "host": {
                                        "owningKey": {
                                            "hash": "CUK712EteZBrQMZ3ecgo6ybqYjWCGyb64fMQ1RjM3wvW"
                                        },
                                        "name": "O=ParticipantA, L=London, C=GB"
                                    },
                                    "dataOutput": "4EgKTmcZ5kro4xu3q5ZPwNGo2THjFRMgDBiNQX9XQkAX",
                                    "flowTopic": "4EgKTmcZ5kro4xu3q5ZPwNGo2THjFRMgDBiNQX9XQkAX",
                                    "fileName": "test1.csv",
                                    "description": "test1 file",
                                    "dataType": "Fixed Income Demand Data",
                                    "dateCreated": "2021-11-05T16:22:17.537Z"
                                },
                                "notary": {
                                    "owningKey": {
                                        "hash": "Dz7Exnx5framzGaZotmsKueS1SeR5oTtQYRaEJynHgq6"
                                    },
                                    "name": "O=Notary, L=London, C=GB"
                                }
                            }
                        }, {
                            "ref": {
                                "index": 0,
                                "txhash": "8161D0CDA5904E213911C7725C9DA411E8A6276F48A68D86EFFC83F5648A1705"
                            },
                            "state": {
                                "constraint": {
                                    "type": "signature",
                                    "key": {
                                        "hash": "CTymWiNxvGWE117QUKSjeW71yaaGc5tNL2i8Zp8dFUvE"
                                    }
                                },
                                "contract": "com.protocol180.aggregator.contract.DataOutputContract",
                                "data": {
                                    "consumer": {
                                        "owningKey": {
                                            "hash": "CUK712EteZBrQMZ3ecgo6ybqYjWCGyb64fMQ1RjM3wvW"
                                        }
                                    },
                                    "host": {
                                        "owningKey": {
                                            "hash": "CUK712EteZBrQMZ3ecgo6ybqYjWCGyb64fMQ1RjM3wvW"
                                        },
                                        "name": "O=ParticipantA, L=London, C=GB"
                                    },
                                    "dataOutput": "4EgKTmcZ5kro4xu3q5ZPwNGo2THjFRMgDBiNQX9XQkAX",
                                    "flowTopic": "4EgKTmcZ5kro4xu3q5ZPwNGo2THjFRMgDBiNQX9XQkAX",
                                    "fileName": "test1.csv",
                                    "description": "test1 file",
                                    "dataType": "Fixed Income Demand Data",
                                    "dateCreated": "2021-11-11T16:22:17.537Z"
                                },
                                "notary": {
                                    "owningKey": {
                                        "hash": "Dz7Exnx5framzGaZotmsKueS1SeR5oTtQYRaEJynHgq6"
                                    },
                                    "name": "O=Notary, L=London, C=GB"
                                }
                            }
                        }, {
                            "ref": {
                                "index": 0,
                                "txhash": "164495907B3F1D99B60583F261E45DF9F601338D2AE3D59FF937C7782BF4F199"
                            },
                            "state": {
                                "constraint": {
                                    "type": "signature",
                                    "key": {
                                        "hash": "CTymWiNxvGWE117QUKSjeW71yaaGc5tNL2i8Zp8dFUvE"
                                    }
                                },
                                "contract": "com.protocol180.aggregator.contract.DataOutputContract",
                                "data": {
                                    "consumer": {
                                        "owningKey": {
                                            "hash": "CUK712EteZBrQMZ3ecgo6ybqYjWCGyb64fMQ1RjM3wvW"
                                        }
                                    },
                                    "host": {
                                        "owningKey": {
                                            "hash": "CUK712EteZBrQMZ3ecgo6ybqYjWCGyb64fMQ1RjM3wvW"
                                        },
                                        "name": "O=ParticipantA, L=London, C=GB"
                                    },
                                    "dataOutput": "4EgKTmcZ5kro4xu3q5ZPwNGo2THjFRMgDBiNQX9XQkAX",
                                    "fileName": "test1.csv",
                                    "flowTopic": "4EgKTmcZ5kro4xu3q5ZPwNGo2THjFRMgDBiNQX9XQkAX",
                                    "description": "test1 file",
                                    "dataType": "Fixed Income Demand Data",
                                    "dateCreated": "2021-11-24T16:22:17.537Z"
                                },
                                "notary": {
                                    "owningKey": {
                                        "hash": "Dz7Exnx5framzGaZotmsKueS1SeR5oTtQYRaEJynHgq6"
                                    },
                                    "name": "O=Notary, L=London, C=GB"
                                }
                            }
                        }],
                    "statesMetadata": [{
                        "constraintInfo": {
                            "constraint": {
                                "type": "signature",
                                "key": {
                                    "hash": "CTymWiNxvGWE117QUKSjeW71yaaGc5tNL2i8Zp8dFUvE"
                                }
                            }
                        },
                        "contractStateClassName": "com.protocol180.aggregator.states.DataOutputState",
                        "notary": {
                            "owningKey": {
                                "hash": "Dz7Exnx5framzGaZotmsKueS1SeR5oTtQYRaEJynHgq6"
                            },
                            "name": "O=Notary, L=London, C=GB"
                        },
                        "recordedTime": "2021-11-23T17:23:55.070Z",
                        "ref": {
                            "index": 0,
                            "txhash": "384EFB8B90124712DBC8F0BD61D8D1120D1A4AA21F6F9D187856B1684BD61FDC"
                        },
                        "relevancyStatus": "RELEVANT",
                        "status": "UNCONSUMED"
                    }, {
                        "constraintInfo": {
                            "constraint": {
                                "type": "signature",
                                "key": {
                                    "hash": "CTymWiNxvGWE117QUKSjeW71yaaGc5tNL2i8Zp8dFUvE"
                                }
                            }
                        },
                        "contractStateClassName": "com.protocol180.aggregator.states.DataOutputState",
                        "notary": {
                            "owningKey": {
                                "hash": "Dz7Exnx5framzGaZotmsKueS1SeR5oTtQYRaEJynHgq6"
                            },
                            "name": "O=Notary, L=London, C=GB"
                        },
                        "recordedTime": "2021-11-24T16:22:17.537Z",
                        "ref": {
                            "index": 0,
                            "txhash": "8161D0CDA5904E213911C7725C9DA411E8A6276F48A68D86EFFC83F5648A1705"
                        },
                        "relevancyStatus": "RELEVANT",
                        "status": "UNCONSUMED"
                    }, {
                        "constraintInfo": {
                            "constraint": {
                                "type": "signature",
                                "key": {
                                    "hash": "CTymWiNxvGWE117QUKSjeW71yaaGc5tNL2i8Zp8dFUvE"
                                }
                            }
                        },
                        "contractStateClassName": "com.protocol180.aggregator.states.DataOutputState",
                        "notary": {
                            "owningKey": {
                                "hash": "Dz7Exnx5framzGaZotmsKueS1SeR5oTtQYRaEJynHgq6"
                            },
                            "name": "O=Notary, L=London, C=GB"
                        },
                        "recordedTime": "2021-11-24T21:29:52.605Z",
                        "ref": {
                            "index": 0,
                            "txhash": "164495907B3F1D99B60583F261E45DF9F601338D2AE3D59FF937C7782BF4F199"
                        },
                        "relevancyStatus": "RELEVANT",
                        "status": "UNCONSUMED"
                    }],
                    "totalStatesAvailable": 3
                };
            });

            this.post("/180 Protocol Broker Flows/DataOutputDecryptFlow", (schema, request) => {
                let attrs = JSON.parse(request.requestBody);
                return {
                    "flowClass": "com.protocol180.aggregator.flows.DataOutputDecryptFlow",
                    "flowRunId": "d237eade-c98a-45f7-9409-0a9bb05c82b9",
                    "result": {
                        "timestamp": "2021-11-24T22:30:22.522Z",
                        "value": [
                            {
                                "fileName": "test1.csv",
                                "description": "test1 file",
                                "dataType": "Fixed Income Demand Data",
                                "data": [{
                                    "creditRating": "AAA",
                                    "sector": "INFRASTRUCTURE",
                                    "assetType": "B",
                                    "duration": "5",
                                    "amount": 538642159
                                }, {
                                    "creditRating": "AAA",
                                    "sector": "INDUSTRIALS",
                                    "assetType": "B",
                                    "duration": "3",
                                    "amount": 929771218
                                }, {
                                    "creditRating": "A",
                                    "sector": "FINANCIALS",
                                    "assetType": "PP",
                                    "duration": "4",
                                    "amount": 69202315
                                }, {
                                    "creditRating": "B",
                                    "sector": "INFRASTRUCTURE",
                                    "assetType": "PP",
                                    "duration": "2",
                                    "amount": 201251157
                                }]
                            },
                            {
                                "fileName": "test2.csv",
                                "description": "test2 file",
                                "dataType": "Fixed Income Demand Data",
                                "data": [{
                                    "creditRating": "AAA",
                                    "sector": "INFRASTRUCTURE",
                                    "assetType": "B",
                                    "duration": "5",
                                    "amount": 538642159
                                }, {
                                    "creditRating": "AAA",
                                    "sector": "INDUSTRIALS",
                                    "assetType": "B",
                                    "duration": "3",
                                    "amount": 929771218
                                }, {
                                    "creditRating": "A",
                                    "sector": "FINANCIALS",
                                    "assetType": "PP",
                                    "duration": "4",
                                    "amount": 69202315
                                }, {
                                    "creditRating": "B",
                                    "sector": "INFRASTRUCTURE",
                                    "assetType": "PP",
                                    "duration": "2",
                                    "amount": 201251157
                                }]
                            }
                        ]
                    },
                    "startedAt": "2021-11-24T22:30:22.037Z"
                };
            });

            this.get("/180Protocol Broker Contracts/RewardsState/query", (schema, request) => {
                return {
                    "stateTypes": "UNCONSUMED",
                    "states": [{
                        "ref": {
                            "index": 0,
                            "txhash": "384EFB8B90124712DBC8F0BD61D8D1120D1A4AA21F6F9D187856B1684BD61FDC"
                        },
                        "state": {
                            "constraint": {
                                "type": "signature",
                                "key": {
                                    "hash": "CTymWiNxvGWE117QUKSjeW71yaaGc5tNL2i8Zp8dFUvE"
                                }
                            },
                            "contract": "com.protocol180.aggregator.contract.RewardsContract",
                            "data": {
                                "provider": {
                                    "owningKey": {
                                        "hash": "CUK712EteZBrQMZ3ecgo6ybqYjWCGyb64fMQ1RjM3wvW"
                                    }
                                },
                                "host": {
                                    "owningKey": {
                                        "hash": "CUK712EteZBrQMZ3ecgo6ybqYjWCGyb64fMQ1RjM3wvW"
                                    },
                                    "name": "O=ParticipantA, L=London, C=GB"
                                },
                                "rewards": "4EgKTmcZ5kro4xu3q5ZPwNGo2THjFRMgDBiNQX9XQkAX",
                                "flowTopic": "4EgKTmcZ5kro4xu3q5ZPwNGo2THjFRMgDBiNQX9XQkAX",
                                "dateCreated": "20/10/2021"
                            },
                            "notary": {
                                "owningKey": {
                                    "hash": "Dz7Exnx5framzGaZotmsKueS1SeR5oTtQYRaEJynHgq6"
                                },
                                "name": "O=Notary, L=London, C=GB"
                            }
                        }
                    }, {
                        "ref": {
                            "index": 0,
                            "txhash": "8161D0CDA5904E213911C7725C9DA411E8A6276F48A68D86EFFC83F5648A1705"
                        },
                        "state": {
                            "constraint": {
                                "type": "signature",
                                "key": {
                                    "hash": "CTymWiNxvGWE117QUKSjeW71yaaGc5tNL2i8Zp8dFUvE"
                                }
                            },
                            "contract": "com.protocol180.aggregator.contract.RewardsContract",
                            "data": {
                                "provider": {
                                    "owningKey": {
                                        "hash": "CUK712EteZBrQMZ3ecgo6ybqYjWCGyb64fMQ1RjM3wvW"
                                    }
                                },
                                "host": {
                                    "owningKey": {
                                        "hash": "CUK712EteZBrQMZ3ecgo6ybqYjWCGyb64fMQ1RjM3wvW"
                                    },
                                    "name": "O=ParticipantA, L=London, C=GB"
                                },
                                "rewards": "4EgKTmcZ5kro4xu3q5ZPwNGo2THjFRMgDBiNQX9XQkAX",
                                "flowTopic": "4EgKTmcZ5kro4xu3q5ZPwNGo2THjFRMgDBiNQX9XQkAX",
                                "dateCreated": "20/10/2021"
                            },
                            "notary": {
                                "owningKey": {
                                    "hash": "Dz7Exnx5framzGaZotmsKueS1SeR5oTtQYRaEJynHgq6"
                                },
                                "name": "O=Notary, L=London, C=GB"
                            }
                        }
                    }, {
                        "ref": {
                            "index": 0,
                            "txhash": "164495907B3F1D99B60583F261E45DF9F601338D2AE3D59FF937C7782BF4F199"
                        },
                        "state": {
                            "constraint": {
                                "type": "signature",
                                "key": {
                                    "hash": "CTymWiNxvGWE117QUKSjeW71yaaGc5tNL2i8Zp8dFUvE"
                                }
                            },
                            "contract": "com.protocol180.aggregator.contract.RewardsContract",
                            "data": {
                                "provider": {
                                    "owningKey": {
                                        "hash": "CUK712EteZBrQMZ3ecgo6ybqYjWCGyb64fMQ1RjM3wvW"
                                    }
                                },
                                "host": {
                                    "owningKey": {
                                        "hash": "CUK712EteZBrQMZ3ecgo6ybqYjWCGyb64fMQ1RjM3wvW"
                                    },
                                    "name": "O=ParticipantA, L=London, C=GB"
                                },
                                "rewards": "4EgKTmcZ5kro4xu3q5ZPwNGo2THjFRMgDBiNQX9XQkAX",
                                "flowTopic": "4EgKTmcZ5kro4xu3q5ZPwNGo2THjFRMgDBiNQX9XQkAX",
                                "dateCreated": "20/10/2021"
                            },
                            "notary": {
                                "owningKey": {
                                    "hash": "Dz7Exnx5framzGaZotmsKueS1SeR5oTtQYRaEJynHgq6"
                                },
                                "name": "O=Notary, L=London, C=GB"
                            }
                        }
                    }],
                    "statesMetadata": [{
                        "constraintInfo": {
                            "constraint": {
                                "type": "signature",
                                "key": {
                                    "hash": "CTymWiNxvGWE117QUKSjeW71yaaGc5tNL2i8Zp8dFUvE"
                                }
                            }
                        },
                        "contractStateClassName": "com.protocol180.aggregator.states.RewardsState",
                        "notary": {
                            "owningKey": {
                                "hash": "Dz7Exnx5framzGaZotmsKueS1SeR5oTtQYRaEJynHgq6"
                            },
                            "name": "O=Notary, L=London, C=GB"
                        },
                        "recordedTime": "2021-11-23T17:23:55.070Z",
                        "ref": {
                            "index": 0,
                            "txhash": "384EFB8B90124712DBC8F0BD61D8D1120D1A4AA21F6F9D187856B1684BD61FDC"
                        },
                        "relevancyStatus": "RELEVANT",
                        "status": "UNCONSUMED"
                    }, {
                        "constraintInfo": {
                            "constraint": {
                                "type": "signature",
                                "key": {
                                    "hash": "CTymWiNxvGWE117QUKSjeW71yaaGc5tNL2i8Zp8dFUvE"
                                }
                            }
                        },
                        "contractStateClassName": "com.protocol180.aggregator.states.RewardsState",
                        "notary": {
                            "owningKey": {
                                "hash": "Dz7Exnx5framzGaZotmsKueS1SeR5oTtQYRaEJynHgq6"
                            },
                            "name": "O=Notary, L=London, C=GB"
                        },
                        "recordedTime": "2021-11-24T16:22:17.537Z",
                        "ref": {
                            "index": 0,
                            "txhash": "8161D0CDA5904E213911C7725C9DA411E8A6276F48A68D86EFFC83F5648A1705"
                        },
                        "relevancyStatus": "RELEVANT",
                        "status": "UNCONSUMED"
                    }, {
                        "constraintInfo": {
                            "constraint": {
                                "type": "signature",
                                "key": {
                                    "hash": "CTymWiNxvGWE117QUKSjeW71yaaGc5tNL2i8Zp8dFUvE"
                                }
                            }
                        },
                        "contractStateClassName": "com.protocol180.aggregator.states.RewardsState",
                        "notary": {
                            "owningKey": {
                                "hash": "Dz7Exnx5framzGaZotmsKueS1SeR5oTtQYRaEJynHgq6"
                            },
                            "name": "O=Notary, L=London, C=GB"
                        },
                        "recordedTime": "2021-11-24T21:29:52.605Z",
                        "ref": {
                            "index": 0,
                            "txhash": "164495907B3F1D99B60583F261E45DF9F601338D2AE3D59FF937C7782BF4F199"
                        },
                        "relevancyStatus": "RELEVANT",
                        "status": "UNCONSUMED"
                    }],
                    "totalStatesAvailable": 3
                };
            });

            this.post("/180 Protocol Broker Flows/RewardsDecryptFlow", (schema, request) => {
                let attrs = JSON.parse(request.requestBody);
                return {
                    "flowClass": "com.protocol180.aggregator.flows.RewardsDecryptFlow",
                    "flowRunId": "d237eade-c98a-45f7-9409-0a9bb05c82b9",
                    "result": {
                        "timestamp": "2021-11-24T22:30:22.522Z",
                        "value": [
                            {
                                "id": "1809",
                                "date": "2021-11-24T22:30:22.522Z",
                                "coApplication": "DMS",
                                "amountProvided": 8.2,
                                "completeness": 8.2,
                                "uniqueness": 8.2,
                                "updateFrequency": 8.2,
                                "qualityScore": 8.2,
                                "rewards": 323
                            },
                            {
                                "id": "1818",
                                "date": "2021-12-01T22:30:22.522Z",
                                "coApplication": "DMS",
                                "amountProvided": 6.2,
                                "completeness": 8.2,
                                "uniqueness": 3.2,
                                "updateFrequency": 8.2,
                                "qualityScore": 5.2,
                                "rewards": 223
                            }
                        ]
                    },
                    "startedAt": "2021-11-24T22:30:22.037Z"
                }
            });
        },
    })

    return server
}