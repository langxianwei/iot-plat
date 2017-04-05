package com.point.iot.base.mysql.jdbc;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.point.iot.base.tools.CommUtils;

public class QueryHelper
{
	Logger log = Logger.getLogger(QueryHelper.class);
    private String primaryKey;
    private String originSQL;
    private Connection connection;
    private ConcurrentHashMap<String, String> oracleMapping;
    
    public void addOracleLookup(final String columnName, final String LookupName) {
        this.oracleMapping.put(columnName, LookupName);
    }
    
    public QueryHelper(final String SQL, final Connection connection) {
        this.primaryKey = "ID";
        this.oracleMapping = new ConcurrentHashMap<String, String>();
        this.originSQL = SQL;
        this.connection = connection;
    }
    /**
     * 执行存储过程
     */
    public synchronized void execCall(List<FunctionParam> paramList){
    	//创建存储过程的对象
        CallableStatement c = null;
		try {
			c = connection.prepareCall(originSQL);
	        //给存储过程的参数设置值
			String s = "";
			for(FunctionParam func : paramList){
				int type = func.getTypes();
				if ( "in".equals(func.getInputType())){
					if ( type == Types.TIMESTAMP){
						c.setTimestamp(func.getIdx(), new Timestamp(CommUtils.parseDate(func.getValue().toString()).getTime()));
					}else if ( type == Types.VARCHAR){
						c.setString(func.getIdx(), func.getValue().toString());
					}else if ( type == Types.FLOAT){
						c.setBigDecimal(func.getIdx(), new BigDecimal(func.getValue().toString()));
					}
				}else if ( "out".equals(func.getInputType())){
					c.registerOutParameter(func.getIdx(), type);
				}
				s += func.getValue() + ",";
			}
			log.debug(s);
	        //执行存储过程
	        c.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				if ( c != null )
					c.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DBConnectionManager.getInstance().freeConnection("yahe", connection);
		}
    }
    
    public Map<String, Object> getJSON() throws SQLException {
        ResultSet rs = null;
        Statement stmt = null;
        final Map<String, Object> result = new HashMap<String, Object>();
        int j = 0;
        try {
            stmt = this.connection.createStatement();
            rs = stmt.executeQuery(this.originSQL);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<HashMap<String, Object>> rowsList = new ArrayList<HashMap<String, Object>>();
           
            while (rs.next()) {
            	j++;
                HashMap<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < columnCount; ++i) {
                    final String columnName = metaData.getColumnName(i + 1);
                    if (!"RN".equals(columnName.toUpperCase())) {
                    	Object obj = rs.getObject(columnName);
                        if ( obj != null) {
                            if (obj instanceof Clob) {
                                final Clob clob = rs.getClob(columnName);
                                String value = clob.getSubString(1L, (int)clob.length());
                                value = new String(value.getBytes(),"utf-8");
                                value = value.replaceAll("\r\n", " ").replaceAll("\n", " ");
                                map.put(columnName, value);
                            }
                            else if (obj instanceof Blob) {
                                final Blob blob = rs.getBlob(columnName);
                                final long len = blob.length();
                                final byte[] data = blob.getBytes(1L, (int)len);
                                map.put(columnName, data);
                            }else if(obj instanceof Timestamp){
                            	Timestamp t = (Timestamp)obj;
                            	map.put(columnName, t);
                            }
                            else {
                                String value2 = obj.toString();
                                value2 = new String(value2.getBytes(),"utf-8");
                                value2 = value2.replaceAll("\r\n", " ").replaceAll("\n", " ");
                                
                                map.put(columnName, value2);
                            }
                        }
                        else {
                            map.put(columnName, "");
                        }
                    }
                }
                rowsList.add(map);
            }
            result.put("data", rowsList);
        }
        catch (SQLException e) {
        	System.out.println(j+ "-----" + this.originSQL);
        	log.error(this.originSQL);
        	log.error(e);
        	connection.close();
        	
        } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            }
            catch (SQLException e2) {
            	log.error("" + j + "---------" + this.originSQL);
                log.debug(e2);
                connection.close();
//                e2.printStackTrace();
            }
        }
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        catch (SQLException e2) {
//        	e2.printStackTrace();
        	log.error("" + j + "---------" + this.originSQL);
            log.debug(e2);
            connection.close();
        }
        return result;
    }
    
    public String getPrimaryKey() {
        return this.primaryKey;
    }
    
    public void setPrimaryKey(final String primaryKey) {
        this.primaryKey = primaryKey;
    }
}
