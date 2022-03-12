package si.microgramm.adyen;

import com.adyen.model.nexo.PrintOutput;

import java.util.List;

public interface AdyenApi {

    void printOnTerminalPrinter(List<PrintOutput> printOutputs) throws AdyenApiException;
}
