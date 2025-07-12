package com.evolutionnext.port.in;


import com.evolutionnext.application.commands.OrderCommand;

public interface ForClientSubmitOrder {
    void submit(OrderCommand orderCommand);
}
