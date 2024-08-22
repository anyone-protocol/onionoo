package org.torproject.metrics.onionoo.onionperf;

import java.sql.Timestamp;
import java.util.List;

public class Measurement {

    private String measurementId;
    private String source;
    private int filesize;
    private Timestamp start;
    private Integer socket;
    private Integer connect;
    private Integer negotiate;
    private Integer request;
    private Integer response;
    private Integer dataRequest;
    private Integer dataResponse;
    private Integer dataComplete;
    private int writeBytes;
    private int readBytes;
    private Boolean didTimeout;
    private Integer partial10240;
    private Integer partial20480;
    private Integer partial51200;
    private Integer partial102400;
    private Integer partial204800;
    private Integer partial512000;
    private Integer partial1048576;
    private Integer partial2097152;
    private Integer partial5242880;
    private Integer dataPerc0;
    private Integer dataPerc10;
    private Integer dataPerc20;
    private Integer dataPerc30;
    private Integer dataPerc40;
    private Integer dataPerc50;
    private Integer dataPerc60;
    private Integer dataPerc70;
    private Integer dataPerc80;
    private Integer dataPerc90;
    private Integer dataPerc100;
    private Timestamp launch;
    private Timestamp usedAt;
    private Integer timeout;
    private Double quantile;
    private Integer circId;
    private Integer usedBy;
    private String endpointLocal;
    private String endpointProxy;
    private String endpointRemote;
    private String hostnameLocal;
    private String hostnameRemote;
    private String sourceAddress;

    private List<BuildTime> buildTimes;

    public Measurement() {
    }

    public Measurement(String measurementId, String source, int filesize, Timestamp start, Integer socket, Integer connect, Integer negotiate, Integer request, Integer response, Integer dataRequest, Integer dataResponse, Integer dataComplete, int writeBytes, int readBytes, Boolean didTimeout, Integer partial10240, Integer partial20480, Integer partial51200, Integer partial102400, Integer partial204800, Integer partial512000, Integer partial1048576, Integer partial2097152, Integer partial5242880, Integer dataPerc0, Integer dataPerc10, Integer dataPerc20, Integer dataPerc30, Integer dataPerc40, Integer dataPerc50, Integer dataPerc60, Integer dataPerc70, Integer dataPerc80, Integer dataPerc90, Integer dataPerc100, Timestamp launch, Timestamp usedAt, Integer timeout, Double quantile, Integer circId, Integer usedBy, String endpointLocal, String endpointProxy, String endpointRemote, String hostnameLocal, String hostnameRemote, String sourceAddress, List<BuildTime> buildTimes) {
        this.measurementId = measurementId;
        this.source = source;
        this.filesize = filesize;
        this.start = start;
        this.socket = socket;
        this.connect = connect;
        this.negotiate = negotiate;
        this.request = request;
        this.response = response;
        this.dataRequest = dataRequest;
        this.dataResponse = dataResponse;
        this.dataComplete = dataComplete;
        this.writeBytes = writeBytes;
        this.readBytes = readBytes;
        this.didTimeout = didTimeout;
        this.partial10240 = partial10240;
        this.partial20480 = partial20480;
        this.partial51200 = partial51200;
        this.partial102400 = partial102400;
        this.partial204800 = partial204800;
        this.partial512000 = partial512000;
        this.partial1048576 = partial1048576;
        this.partial2097152 = partial2097152;
        this.partial5242880 = partial5242880;
        this.dataPerc0 = dataPerc0;
        this.dataPerc10 = dataPerc10;
        this.dataPerc20 = dataPerc20;
        this.dataPerc30 = dataPerc30;
        this.dataPerc40 = dataPerc40;
        this.dataPerc50 = dataPerc50;
        this.dataPerc60 = dataPerc60;
        this.dataPerc70 = dataPerc70;
        this.dataPerc80 = dataPerc80;
        this.dataPerc90 = dataPerc90;
        this.dataPerc100 = dataPerc100;
        this.launch = launch;
        this.usedAt = usedAt;
        this.timeout = timeout;
        this.quantile = quantile;
        this.circId = circId;
        this.usedBy = usedBy;
        this.endpointLocal = endpointLocal;
        this.endpointProxy = endpointProxy;
        this.endpointRemote = endpointRemote;
        this.hostnameLocal = hostnameLocal;
        this.hostnameRemote = hostnameRemote;
        this.sourceAddress = sourceAddress;
        this.buildTimes = buildTimes;
    }

    public String getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(String measurementId) {
        this.measurementId = measurementId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getFilesize() {
        return filesize;
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    public Timestamp getStart() {
        return start;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public Integer getSocket() {
        return socket;
    }

    public void setSocket(Integer socket) {
        this.socket = socket;
    }

    public Integer getConnect() {
        return connect;
    }

    public void setConnect(Integer connect) {
        this.connect = connect;
    }

    public Integer getNegotiate() {
        return negotiate;
    }

    public void setNegotiate(Integer negotiate) {
        this.negotiate = negotiate;
    }

    public Integer getRequest() {
        return request;
    }

    public void setRequest(Integer request) {
        this.request = request;
    }

    public Integer getResponse() {
        return response;
    }

    public void setResponse(Integer response) {
        this.response = response;
    }

    public Integer getDataRequest() {
        return dataRequest;
    }

    public void setDataRequest(Integer dataRequest) {
        this.dataRequest = dataRequest;
    }

    public Integer getDataResponse() {
        return dataResponse;
    }

    public void setDataResponse(Integer dataResponse) {
        this.dataResponse = dataResponse;
    }

    public Integer getDataComplete() {
        return dataComplete;
    }

    public void setDataComplete(Integer dataComplete) {
        this.dataComplete = dataComplete;
    }

    public int getWriteBytes() {
        return writeBytes;
    }

    public void setWriteBytes(int writeBytes) {
        this.writeBytes = writeBytes;
    }

    public int getReadBytes() {
        return readBytes;
    }

    public void setReadBytes(int readBytes) {
        this.readBytes = readBytes;
    }

    public Boolean getDidTimeout() {
        return didTimeout;
    }

    public void setDidTimeout(Boolean didTimeout) {
        this.didTimeout = didTimeout;
    }

    public Integer getPartial10240() {
        return partial10240;
    }

    public void setPartial10240(Integer partial10240) {
        this.partial10240 = partial10240;
    }

    public Integer getPartial20480() {
        return partial20480;
    }

    public void setPartial20480(Integer partial20480) {
        this.partial20480 = partial20480;
    }

    public Integer getPartial51200() {
        return partial51200;
    }

    public void setPartial51200(Integer partial51200) {
        this.partial51200 = partial51200;
    }

    public Integer getPartial102400() {
        return partial102400;
    }

    public void setPartial102400(Integer partial102400) {
        this.partial102400 = partial102400;
    }

    public Integer getPartial204800() {
        return partial204800;
    }

    public void setPartial204800(Integer partial204800) {
        this.partial204800 = partial204800;
    }

    public Integer getPartial512000() {
        return partial512000;
    }

    public void setPartial512000(Integer partial512000) {
        this.partial512000 = partial512000;
    }

    public Integer getPartial1048576() {
        return partial1048576;
    }

    public void setPartial1048576(Integer partial1048576) {
        this.partial1048576 = partial1048576;
    }

    public Integer getPartial2097152() {
        return partial2097152;
    }

    public void setPartial2097152(Integer partial2097152) {
        this.partial2097152 = partial2097152;
    }

    public Integer getPartial5242880() {
        return partial5242880;
    }

    public void setPartial5242880(Integer partial5242880) {
        this.partial5242880 = partial5242880;
    }

    public Integer getDataPerc0() {
        return dataPerc0;
    }

    public void setDataPerc0(Integer dataPerc0) {
        this.dataPerc0 = dataPerc0;
    }

    public Integer getDataPerc10() {
        return dataPerc10;
    }

    public void setDataPerc10(Integer dataPerc10) {
        this.dataPerc10 = dataPerc10;
    }

    public Integer getDataPerc20() {
        return dataPerc20;
    }

    public void setDataPerc20(Integer dataPerc20) {
        this.dataPerc20 = dataPerc20;
    }

    public Integer getDataPerc30() {
        return dataPerc30;
    }

    public void setDataPerc30(Integer dataPerc30) {
        this.dataPerc30 = dataPerc30;
    }

    public Integer getDataPerc40() {
        return dataPerc40;
    }

    public void setDataPerc40(Integer dataPerc40) {
        this.dataPerc40 = dataPerc40;
    }

    public Integer getDataPerc50() {
        return dataPerc50;
    }

    public void setDataPerc50(Integer dataPerc50) {
        this.dataPerc50 = dataPerc50;
    }

    public Integer getDataPerc60() {
        return dataPerc60;
    }

    public void setDataPerc60(Integer dataPerc60) {
        this.dataPerc60 = dataPerc60;
    }

    public Integer getDataPerc70() {
        return dataPerc70;
    }

    public void setDataPerc70(Integer dataPerc70) {
        this.dataPerc70 = dataPerc70;
    }

    public Integer getDataPerc80() {
        return dataPerc80;
    }

    public void setDataPerc80(Integer dataPerc80) {
        this.dataPerc80 = dataPerc80;
    }

    public Integer getDataPerc90() {
        return dataPerc90;
    }

    public void setDataPerc90(Integer dataPerc90) {
        this.dataPerc90 = dataPerc90;
    }

    public Integer getDataPerc100() {
        return dataPerc100;
    }

    public void setDataPerc100(Integer dataPerc100) {
        this.dataPerc100 = dataPerc100;
    }

    public Timestamp getLaunch() {
        return launch;
    }

    public void setLaunch(Timestamp launch) {
        this.launch = launch;
    }

    public Timestamp getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Timestamp usedAt) {
        this.usedAt = usedAt;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Double getQuantile() {
        return quantile;
    }

    public void setQuantile(Double quantile) {
        this.quantile = quantile;
    }

    public Integer getCircId() {
        return circId;
    }

    public void setCircId(Integer circId) {
        this.circId = circId;
    }

    public Integer getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(Integer usedBy) {
        this.usedBy = usedBy;
    }

    public String getEndpointLocal() {
        return endpointLocal;
    }

    public void setEndpointLocal(String endpointLocal) {
        this.endpointLocal = endpointLocal;
    }

    public String getEndpointProxy() {
        return endpointProxy;
    }

    public void setEndpointProxy(String endpointProxy) {
        this.endpointProxy = endpointProxy;
    }

    public String getEndpointRemote() {
        return endpointRemote;
    }

    public void setEndpointRemote(String endpointRemote) {
        this.endpointRemote = endpointRemote;
    }

    public String getHostnameLocal() {
        return hostnameLocal;
    }

    public void setHostnameLocal(String hostnameLocal) {
        this.hostnameLocal = hostnameLocal;
    }

    public String getHostnameRemote() {
        return hostnameRemote;
    }

    public void setHostnameRemote(String hostnameRemote) {
        this.hostnameRemote = hostnameRemote;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public List<BuildTime> getBuildTimes() {
        return buildTimes;
    }

    public void setBuildTimes(List<BuildTime> buildTimes) {
        this.buildTimes = buildTimes;
    }
}
