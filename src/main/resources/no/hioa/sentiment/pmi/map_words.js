function printDistance() {	
	var words = this.content.replace(/[^a-zA-Z������\s]/g,'').split(' ');
	emit(1, words.length - 1);	
}