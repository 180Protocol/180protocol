import React from 'react';
import {ResponsivePie} from '@nivo/pie'

const MyResponsivePie = ({data}) => (
    <ResponsivePie
        data={data}
        margin={{top: 40, right: 80, bottom: 80, left: 80}}
        innerRadius={0}
        padAngle={0.7}
        cornerRadius={3}
        activeOuterRadiusOffset={8}
        borderWidth={1}
        borderColor={{from: 'color', modifiers: [['darker', 0.2]]}}
        enableArcLinkLabels={false}
        enableArcLabels={false}
        colors={{datum: 'data.color'}}
    />
)

export default MyResponsivePie;