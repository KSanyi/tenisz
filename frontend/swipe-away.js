const trackElement = function(trackElement, menuElement, callback) {
  let startX = null;
  let dx = 0;

  trackElement.addEventListener("pointerdown", e => {
    if (window.innerWidth > 1024) {
      // CSS defines a hideable menu only used for smaller screens
      return;
    }
    startX = e.clientX;
    dx = 0;
    menuElement.origTransition = menuElement.style.transition;
    menuElement.style.transition = "none";
    menuElement.origMarginLeft = menuElement.style.marginLeft;
    menuElement.style.marginLeft = "0px";
    trackElement.setPointerCapture(e.pointerId);
  });

  trackElement.addEventListener("pointermove", e => {
    if (startX === null) {
      return;
    }
    dx = e.clientX - startX;
    const marginLeft = Math.min(0, dx);
    menuElement.style.marginLeft = marginLeft + "px";
  });

  const end = () => {
    if (startX === null) {
      return;
    }
    startX = null;

    menuElement.style.transition = menuElement.origTransition;
    delete menuElement.origTransition;
    menuElement.style.marginLeft = menuElement.origMarginLeft;
    delete menuElement.origMarginLeft;

    if (dx < -50) {
      callback({dx});
    }
    dx = 0;
  };

  trackElement.addEventListener("pointerup", end);
  trackElement.addEventListener("pointercancel", end);
};

window.addSwipeAway = function(
  menuElement,
  callbackContainer,
  callbackMethod,
  additionalElement
) {
  trackElement(menuElement, menuElement, function(details) {
    callbackContainer.$server[callbackMethod](details);
  });
  trackElement(additionalElement, menuElement, function(details) {
    callbackContainer.$server[callbackMethod](details);
  });
};
