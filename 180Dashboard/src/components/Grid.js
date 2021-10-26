import React, {useState, useEffect} from 'react';
import {
    SortingState, IntegratedSorting, DataTypeProvider
} from '@devexpress/dx-react-grid';
import {
    Grid,
    Table,
    TableHeaderRow
} from '@devexpress/dx-react-grid-bootstrap4';

export default (props) => {
    const [columns, setColumns] = useState([]);
    const [rows, setRows] = useState([]);
    const [amountColumns] = useState(['rewardsBalance']);
    const [decimalColumns] = useState(['qualityScore']);

    const [sorting, setSorting] = useState([{columnName: 'coApp', direction: 'desc'}]);

    useEffect(() => {
        if (props.columns) {
            setColumns(props.columns);
        }
        if (props.rows) {
            setRows(props.rows);
        }
    }, [props.columns, props.rows]);

    const AmountFormatter = ({value}) => value ? Intl.NumberFormat().format(value.toFixed(1)) : '$0';
    const DecimalsFormatter = ({value}) => value ? value.toFixed(1) : '';

    return (
        <div className="card">
            <Grid rows={rows} columns={columns}>
                <DataTypeProvider
                    for={amountColumns}
                    formatterComponent={AmountFormatter}
                    {...props}
                />
                <DataTypeProvider
                    for={decimalColumns}
                    formatterComponent={DecimalsFormatter}
                    {...props}
                />
                <SortingState
                    mode={'multiple'}
                    sorting={sorting}
                    onSortingChange={setSorting}
                />
                <IntegratedSorting/>
                <Table/>
                <TableHeaderRow showSortingControls/>
            </Grid>
        </div>
    );
};
