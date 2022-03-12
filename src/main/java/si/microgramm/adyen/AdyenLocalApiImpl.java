package si.microgramm.adyen;

import com.adyen.Client;
import com.adyen.Config;
import com.adyen.builders.terminal.TerminalAPIRequestBuilder;
import com.adyen.model.nexo.PrintOutput;
import com.adyen.model.nexo.PrintRequest;
import com.adyen.model.terminal.TerminalAPIRequest;
import com.adyen.model.terminal.security.SecurityKey;
import com.adyen.service.TerminalLocalAPI;
import si.microgramm.adyen.common.ServiceIdGenerator;

import java.util.List;

public class AdyenLocalApiImpl implements AdyenApi {

    private final TerminalLocalAPI localApi;
    private final String cashRegisterId;
    private final String terminalId;
    private final SecurityKey securityKey;

    public AdyenLocalApiImpl(Config config, SecurityKey securityKey, String cashRegisterId, String terminalId) {
        this.securityKey = securityKey;
        this.cashRegisterId = cashRegisterId;
        this.terminalId = terminalId;
        localApi = new TerminalLocalAPI(new Client(config));
    }

    @Override
    public void printOnTerminalPrinter(List<PrintOutput> printOutputs) throws AdyenApiException {
        try {
            for (PrintOutput printOutput : printOutputs) {
                PrintRequest printRequest = new PrintRequest();
                printRequest.setPrintOutput(printOutput);

                TerminalAPIRequest request = getRequestBuilder()
                        .withPrintRequest(printRequest)
                        .build();

                localApi.request(request, securityKey);
            }
        } catch (Exception e) {
            throw new AdyenApiException(e);
        }
    }

    private TerminalAPIRequestBuilder getRequestBuilder() {
        return getRequestBuilder(ServiceIdGenerator.getInstance().generate());
    }

    private TerminalAPIRequestBuilder getRequestBuilder(String serviceId) {
        return new TerminalAPIRequestBuilder(
                cashRegisterId,
                serviceId,
                terminalId);
    }
}
