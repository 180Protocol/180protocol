import React, {useState, useEffect} from 'react';
import {
    SortingState, IntegratedSorting, PagingState, IntegratedPaging, DataTypeProvider
} from '@devexpress/dx-react-grid';
import {
    Grid,
    Table,
    TableHeaderRow,
    PagingPanel
} from '@devexpress/dx-react-grid-bootstrap4';
import moment from "moment";

export default (props) => {
    const [columns, setColumns] = useState([]);
    const [rows, setRows] = useState([]);
    const [amountColumns] = useState(['rewardsBalance']);
    const [decimalColumns] = useState(['qualityScore']);
    const [dateColumns] = useState(['date']);

    const [sorting, setSorting] = useState([{columnName: 'coApp', direction: 'desc'}]);
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [pageSizes] = useState([10, 25, 100]);

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
    const DateFormatter = ({value}) => value ? moment.utc(value).format("YYYY-MM-DD HH:mm:ss") : '';

    const FlowIDCell = ({value, style, ...restProps}) => {
        return <Table.Cell
            {...restProps}
        >
            <div>
                 <span title={value}>
                    {value}
                </span>
            </div>
        </Table.Cell>
    };

    const Cell = (props) => {
        const {column} = props;
        if (column.name === 'flowId' && !props.row.matches) {
            return <FlowIDCell  {...props} />;
        }
        return <Table.Cell {...props} />;
    };

    return (
        <div className="card">
            <Grid rows={rows} columns={columns}>
                <DataTypeProvider
                    for={amountColumns}
                    formatterComponent={AmountFormatter}
                    {...props}
                />
                <DataTypeProvider
                    for={dateColumns}
                    formatterComponent={DateFormatter}
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
                <PagingState
                    currentPage={currentPage}
                    onCurrentPageChange={setCurrentPage}
                    pageSize={pageSize}
                    onPageSizeChange={setPageSize}
                />
                <IntegratedPaging/>
                <Table cellComponent={Cell}/>
                <TableHeaderRow showSortingControls/>
                <PagingPanel
                    pageSizes={pageSizes}
                />
            </Grid>
        </div>
    );
};
