function printDistance() {	
	var word = '%WORD%'	
	var words = this.content.replace(/[^a-zA-ZøæåØÆÅ\s]/g,'').split(' ');		
	for ( var i = 0; i < words.length; i++) {
		var tmpWord = words[i].toLowerCase();
		if (tmpWord == word) {
			emit(1, 1);
		}		
	}	
}