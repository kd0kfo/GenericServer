import com.davecoss.android.genericserver.*;

public class plugintest implements com.davecoss.android.genericserver.Module {

	public plugintest() {
		//
	}

	@Override
	public HTTPReply process_request(HTTPRequest request) {
		
		return new HTMLReply("Plugin Reply", "You asked for " + request.get_uri());
	}

}
