
NumberTextField = Ember.TextField.extend({
    tagName: "NotEmptyTextField",
    // implementation of this function, see http://stackoverflow.com/a/995193/65542
    keyDown: function(event) {
        // Allow: backspace, delete, tab, escape, and enter
        if (event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 27 || event.keyCode == 13 ||
        // Allow: Ctrl+A
        (event.keyCode == 65 && event.ctrlKey === true) ||
        // Allow: home, end, left, right
        (event.keyCode >= 35 && event.keyCode <= 39)) {
            // let it happen, don't do anything
            return;
        }
        else {
            // Ensure that it is a number and stop the keypress
            if (event.shiftKey || (event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105)) {
                event.preventDefault();
            }
        }
    },
    validate: function() {
            return true;
    }
});



EmptyDifferTextField = Ember.TextField.extend({
    attributeBindings: ['style'],
    tagName: "EmptyDifferTextField",
    keyDown: function(event) {
        // Allow: backspace, delete, tab, escape, and enter
        if (event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 27 || event.keyCode == 13 ||
        // Allow: Ctrl+A
        (event.keyCode == 65 && event.ctrlKey === true) ||
        // Allow: home, end, left, right
        (event.keyCode >= 35 && event.keyCode <= 39)) {
            // let it happen, don't do anything
            return;
        }
        else {
            // Ensure that it is a number and stop the keypress
        }

    },
    validate: function() {
             return this.focusOut(null);
    },
    focusOut: function(event) {
       try {
            this.set('value', this.get('value').trim());
       } catch (e) {
            this.set('value',"");
       }
       if (this.get('value').length <= 0) {
            this.$().addClass("success").removeClass("error");
            this.$().attr('title',null);
            $("#" + this.$().attr('id')).tooltip("enable");
            this.$().attr('rel','tooltip');

            try {
                this.set('isValidOnFocusOutCallback',false);
            } catch (ee) {

            }
            return false;
       } else {

            if (this.get('compare') != null) {

                 if (this.get('compare')!=this.get('value')) {

                                console.error('SAME');
                                this.$().addClass("success").removeClass("error");
                                 this.$().attr('title',null);
                                 $("#" + this.$().attr('id')).tooltip("destroy");
                                 this.$().attr('rel','');

                                 try {
                                     this.set('isValidOnFocusOutCallback',false);
                                 } catch (ee) {

                                 }
                                 return true;
                 } else {
                                console.error('NOT SAME');
                                 this.$().addClass("error").removeClass("success");
                                 this.$().attr('title','The two fields "username" must not match.');
                                 this.$().attr('rel','');
                                  $("#" + this.$().attr('id')).tooltip("enable");

                                 try {
                                     this.set('isValidOnFocusOutCallback',true);
                                 } catch (ee) {

                                 }
                                 return false;
                 }
            } else {
                this.$().addClass("success").removeClass("error");
                this.$().attr('title',null);
                 $("#" + this.$().attr('id')).tooltip("destroy");
                this.$().attr('rel','');

                try {
                    this.set('isValidOnFocusOutCallback',false);
                } catch (ee) {

                }
                return true;
            }

       }
    }
});
