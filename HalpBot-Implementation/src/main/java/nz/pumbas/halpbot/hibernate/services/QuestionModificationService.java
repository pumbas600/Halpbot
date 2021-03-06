/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot.hibernate.services;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import nz.pumbas.halpbot.hibernate.exceptions.ResourceNotFoundException;
import nz.pumbas.halpbot.hibernate.models.QuestionModification;
import nz.pumbas.halpbot.hibernate.repositories.QuestionModificationRepository;

public class QuestionModificationService
{
    private final Set<Long> ids = new HashSet<>();
    private final List<Consumer<QuestionModification>> listeners = new ArrayList<>();
    private static final long CHECK_FOR_NEW_MODIFICATION_INTERVAL = 5;

    private final QuestionModificationRepository questionModificationRepository;

    public QuestionModificationService(QuestionModificationRepository questionModificationRepository) {
        this.questionModificationRepository = questionModificationRepository;
        this.ids.addAll(this.questionModificationRepository.getAllIds());
        //this.startRepositoryChecks();
    }

    public boolean existsById(@Nullable Long id) {
        //return null != id && this.questionModificationRepository.existsById(id);
        return false;
    }

    public List<QuestionModification> listFirstAmount(int amount) {
        List<QuestionModification> questions = new ArrayList<>();
//        Pageable pageable = PageRequest.of(1, amount);
//        this.questionModificationRepository.findAll(pageable).forEach(questions::add);
        return questions;
    }

    public void deleteById(@Nullable Long id) throws ResourceNotFoundException {
        if (!this.existsById(id)) {
            throw new ResourceNotFoundException("Cannot find question modification with id: " + id);
        }
        //this.questionModificationRepository.deleteById(id);
        this.ids.remove(id);
    }

    public void deleteAll() {
        //this.questionModificationRepository.deleteAll();
        this.ids.clear();
    }

    public List<QuestionModification> list() {
        //return this.questionModificationRepository.findAll();
        return new ArrayList<>();
    }

    public long count() {
        //return this.questionModificationRepository.count();
        return 0;
    }

    private void startRepositoryChecks() {
//        HalpbotUtils.context().get(ConcurrentManager.class)
//            .scheduleRegularly(
//                QuestionModificationService.CHECK_FOR_NEW_MODIFICATION_INTERVAL,
//                QuestionModificationService.CHECK_FOR_NEW_MODIFICATION_INTERVAL,
//                TimeUnit.MINUTES, this::updateIds);
    }

    public QuestionModificationService addListener(Consumer<QuestionModification> listener) {
        this.listeners.add(listener);
        return this;
    }

    public void updateIds() {
        if (this.count() == this.ids.size())
            return;

        this.questionModificationRepository.getAllNotInIds(this.ids)
            .forEach(question -> {
                this.listeners.forEach(listener -> listener.accept(question));
                this.ids.add(question.getId());
            });
    }
}
