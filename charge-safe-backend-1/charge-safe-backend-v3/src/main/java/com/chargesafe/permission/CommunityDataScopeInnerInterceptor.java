package com.chargesafe.permission;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.community.entity.Community;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.inspection.entity.InspectionTask;
import com.chargesafe.modules.repair.entity.RepairReport;
import com.chargesafe.modules.charging.entity.ChargingReservation;
import com.chargesafe.modules.charging.entity.ChargingOrder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class CommunityDataScopeInnerInterceptor implements InnerInterceptor {

    private static final Set<String> COMMUNITY_SCOPE_TABLES = new HashSet<>(Arrays.asList(
            "community", "parking_area",
            "inspection_task", "repair_report",
            "charging_reservation", "charging_order"
    ));

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        if (!CommunityDataScopeAspect.isScopeActive()) return;
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(ms.getId())) return;

        Long communityId = CommunityDataScopeAspect.getCurrentCommunityId();
        if (communityId == null) return;
        if (ms.getSqlCommandType() != SqlCommandType.SELECT) return;

        String originalSql = boundSql.getSql();
        try {
            Select select = (Select) CCJSqlParserUtil.parse(originalSql);
            if (!(select.getSelectBody() instanceof PlainSelect)) return;

            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            Table table = (Table) plainSelect.getFromItem();
            if (table == null) return;

            String tableName = table.getName().toLowerCase().replace("`", "");
            if (!COMMUNITY_SCOPE_TABLES.contains(tableName)) return;

            Expression where = plainSelect.getWhere();
            Expression dataScopeExpr = new EqualsTo()
                    .withLeftExpression(getColumnForTable(tableName))
                    .withRightExpression(new LongValue(communityId));

            if (where == null) {
                plainSelect.setWhere(dataScopeExpr);
            } else {
                plainSelect.setWhere(new AndExpression(where, dataScopeExpr));
            }

            PluginUtils.mpBoundSql(boundSql).sql(select.toString());
            log.debug("数据权限SQL注入: table={}, communityId={}", tableName, communityId);
        } catch (JSQLParserException e) {
            log.warn("数据权限SQL解析失败: {}", e.getMessage());
        }
    }

    private Column getColumnForTable(String tableName) {
        if ("community".equals(tableName)) return new Column("id");
        return new Column("community_id");
    }

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {}

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {}
}
